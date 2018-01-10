package org.jahia.modules.databaseConnector.connection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.modules.databaseConnector.bundle.RBExecutor;
import org.jahia.modules.databaseConnector.connector.ConnectorMetaData;
import org.jahia.modules.databaseConnector.services.DatabaseConnectionRegistry;
import org.jahia.modules.databaseConnector.util.Utils;
import org.jahia.modules.databaseConnector.dsl.DSLExecutor;
import org.jahia.modules.databaseConnector.dsl.DSLHandler;
import org.jahia.osgi.BundleResource;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ParseException;
import org.jahia.services.render.*;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.EncryptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

/**
 * Date: 2013-10-17
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class DatabaseConnectorManager implements InitializingBean, BundleListener, BundleContextAware {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectorManager.class);

    public static final String DATABASE_CONNECTOR_ROOT_PATH = "/settings/";
    public static final String DATABASE_CONNECTOR_PATH = "databaseConnector";
    public static final String DATABASE_CONNECTOR_NODE_TYPE = "dc:databaseConnector";

    private static final String DEFINITION_QUERY = "SELECT * FROM [dcmix:directivesDefinition] AS result WHERE ISDESCENDANTNODE(result, ''{0}'')";
    private static final String SERVICES_QUERY = "SELECT * FROM [dcmix:servicesDefinition] AS result WHERE ISDESCENDANTNODE(result, ''{0}'')";

    private static final String SERVICE_VIEW_NAME = "service";

    private static DatabaseConnectorManager instance;
    private DSLExecutor dslExecutor;
    private Map<String, DSLHandler> dslHandlerMap;
    private final static Set<Long> installedBundles = new LinkedHashSet<>();
    private final static Map<String, String> angularConfigFilesPath = new HashMap<>();
    private final static Map<String, Long> angularConfigFilesTimestamp = new HashMap<>();
    private SettingsBean settingsBean;
    private JCRTemplate jcrTemplate;
    private RenderService renderService;
    private JahiaTemplateManagerService templateManagerService;
    private JahiaUserManagerService userManagerService;
    private Bundle bundle;
    private RBExecutor rbExecutor;
    private static Date lastDeployDate;
    private static Date lastBundleEvent;
    private static String DCMIX_DIRECTIVES_DEFINITION = "dcmix:directivesDefinition";
    private static String DCMIX_SERVICES_DEFINITION = "dcmix:servicesDefinition";
    private final static ConcurrentMap<String, Semaphore> processings = new ConcurrentHashMap<>();
    public static DatabaseConnectorManager getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectorManager();
            instance.userManagerService = JahiaUserManagerService.getInstance();

            if (instance.jcrTemplate == null) {
                instance.jcrTemplate = JCRTemplate.getInstance();
            }

            instance.rbExecutor = new RBExecutor(instance.jcrTemplate);
        }
        return instance;
    }

    @Override
    public void bundleChanged(BundleEvent bundleEvent) {
        Bundle bundleEventBundle = bundleEvent.getBundle();
        if (bundleEvent.getType() == BundleEvent.INSTALLED || bundleEvent.getType() == BundleEvent.UPDATED ||
                bundleEvent.getType() == BundleEvent.STARTED || bundleEvent.getType() == BundleEvent.STOPPED ||
                bundleEvent.getType() == BundleEvent.RESOLVED) {
            lastBundleEvent = new Date();
        }
        if (settingsBean != null && settingsBean.isProcessingServer()) {
            if (bundleEventBundle.getSymbolicName().contains("connector")) {
                logger.debug("Processing bundle: [" + bundleEventBundle.getSymbolicName() + "]" + " - Current Bundle Status: [" + Utils.resolveBundleName(bundleEvent.getType()) + "]");
            }
            long bundleId = bundleEvent.getBundle().getBundleId();
            if (bundleEvent.getType() == BundleEvent.INSTALLED || bundleEvent.getType() == BundleEvent.UPDATED) {
                installedBundles.add(bundleId);
            }

            if ((bundleEvent.getType() == BundleEvent.RESOLVED && installedBundles.contains(bundleId)) || (bundleEventBundle.getState() == Bundle.RESOLVED && bundleEvent.getType() == BundleEvent.INSTALLED) || (bundleEventBundle.getState() == BundleEvent.RESOLVED && bundleEvent.getType() == BundleEvent.STARTED)) {
                installedBundles.remove(bundleId);
                try {
                    logger.debug("Preparing to Parse definitions");
                    parseDefinitionWizards(bundleEvent.getBundle());
                } catch (ParseException e) {
                    logger.error("Parse exception: " + e.getMessage());
                } catch (IOException e) {
                    logger.error("IO exception: " + e.getMessage());
                }
            }
        } else {
            if (settingsBean == null) {
                logger.debug("Settings bean is null...Skipping bundle: [" + bundleEvent.getBundle().getSymbolicName() + "]" + "\n\t - Current Bundle Status: [" + Utils.resolveBundleName(bundleEvent.getType()) + "]");
            } else {
                logger.debug("This is not a processing server... No action will be taken for bundle: [" + bundleEvent.getBundle().getSymbolicName() + "]" + "\n\t - Current Bundle Status: [" + Utils.resolveBundleName(bundleEvent.getType()) + "]");
            }
        }
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        bundleContext.addBundleListener(this);
        this.bundle = bundleContext.getBundle();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Preparing to process bundles after properties set of database connector module.");
        lastDeployDate = new Date();
        lastBundleEvent = new Date();
        for (Bundle currentBundle : this.bundle.getBundleContext().getBundles()) {
            if (currentBundle.getSymbolicName().contains("connector")) {
                logger.debug("Processing bundle: [" + currentBundle.getSymbolicName() + "]" + " - Current Bundle Status: [" + Utils.resolveBundleName(currentBundle.getState()) + "]");
            }
            if (!(currentBundle.getSymbolicName().equals(this.bundle.getSymbolicName()))
                    && org.jahia.osgi.BundleUtils.isJahiaModuleBundle(currentBundle)
                    && (currentBundle.getState() == Bundle.INSTALLED
                    || currentBundle.getState() == Bundle.RESOLVED
                    || currentBundle.getState() == Bundle.ACTIVE)) {
                parseDefinitionWizards(currentBundle);
            }
        }
        logger.debug("Preparing to Parse [" + this.bundle.getSymbolicName() + "] directive definitions");
        parseDefinitionWizards(this.bundle);
    }

    public <T extends AbstractConnection> Map<String, T> getConnections(String databaseType) throws InstantiationException, IllegalAccessException {
        return findRegisteredConnections().get(databaseType);
    }

    public <T extends AbstractConnection, E extends AbstractDatabaseConnectionRegistry> Map<String, Map> findRegisteredConnections() throws InstantiationException, IllegalAccessException {
        Map<String, Map> registeredConnections = new HashMap<>();

        for (DatabaseConnectionRegistry databaseConnectionRegistry : getDatabaseConnectionRegistryServices()) {
            String connectionType = databaseConnectionRegistry.getConnectionType();
            Map<String, T> registry = databaseConnectionRegistry.getRegistry();
            Map<String, T> connectionMap = new HashMap<>();
            if (registry != null) {
                for (Map.Entry<String, T> registryEntry : registry.entrySet()) {
                    connectionMap.put(registryEntry.getKey(), registryEntry.getValue());
                }
            }
            registeredConnections.put(connectionType, connectionMap);
        }
        return registeredConnections;
    }

    public <T extends AbstractConnection> T getConnection(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException {
        try {
            Map<String, T> databaseConnections = getConnections(databaseType);
            return databaseConnections.get(connectionId);
        } catch (NullPointerException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public <T extends AbstractConnection> boolean hasConnection(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException {
        Map<String, T> databaseConnections = getConnections(databaseType);
        return databaseConnections.containsKey(connectionId);
    }

    public boolean addEditConnection(final AbstractConnection connection, final Boolean isEdition) {
        DatabaseConnectionRegistry databaseConnectionRegistry = getDatabaseConnectionRegistryService(connection.getDatabaseType());
        return databaseConnectionRegistry.addEditConnection(connection, isEdition);
    }

    public boolean removeConnection(String connectionId, String databaseType) {
        DatabaseConnectionRegistry databaseConnectionRegistry = getDatabaseConnectionRegistryService(databaseType);
        return databaseConnectionRegistry.removeConnection(connectionId);
    }

    public boolean updateConnection(String connectionId, String databaseType, boolean connect) {
        DatabaseConnectionRegistry databaseConnectionRegistry = getDatabaseConnectionRegistryService(databaseType);
        if (connect) {
            if (((AbstractConnection) databaseConnectionRegistry.getRegistry().get((connectionId))).testConnectionCreation()) {
                databaseConnectionRegistry.connect(connectionId);
            } else {
                return false;
            }
        } else {
            databaseConnectionRegistry.disconnect(connectionId);
        }
        return true;
    }

    public boolean testConnection(AbstractConnection connection) {
        DatabaseConnectionRegistry databaseConnectionRegistry = getDatabaseConnectionRegistryService(connection.getDatabaseType());
        return databaseConnectionRegistry.testConnection(connection);
    }

    public Map executeConnectionImportHandler(InputStream source) {
        File file = null;
        Map<String, Map> parsedConnections = new LinkedHashMap<>();
        Map<String, Map> importedConnections = new LinkedHashMap<>();
        Map<String, String> report = new LinkedHashMap<>();
        try {
            file = File.createTempFile("temporaryImportFile", ".wzd");
            FileUtils.copyInputStreamToFile(source, file);
            dslExecutor.execute(file.toURI().toURL(), dslHandlerMap.get("importConnection"), parsedConnections);
            for (Map.Entry<String, ConnectorMetaData> entry : getAvailableConnectors().entrySet()) {
                String databaseType = entry.getValue().getDatabaseType();
                if (parsedConnections.containsKey(databaseType)) {
                    Map<String, List> results = new LinkedHashMap<>();
                    List<Map> validConnections = new LinkedList();
                    List<Map> failedConnections = new LinkedList();
                    for (Map connectionConfiguration : (LinkedList<Map>) parsedConnections.get(databaseType)) {
                        connectionConfiguration = importConnection(connectionConfiguration);
                        if (connectionConfiguration.get("status").equals("success")) {
                            validConnections.add(connectionConfiguration);
                        } else {
                            failedConnections.add(connectionConfiguration);
                        }
                    }
                    results.put("success", validConnections);
                    results.put("failed", failedConnections);
                    importedConnections.put(databaseType, results);
                }
            }
            logger.info("Done importing connections" + parsedConnections);
            report.put("status", "success");
        } catch (FileNotFoundException ex) {
            report.put("status", "error");
            report.put("reason", "fileNotFound");
            logger.error(ex.getMessage(), ex);
        } catch (IOException ex) {
            report.put("status", "error");
            report.put("reason", "io");
            logger.error(ex.getMessage(), ex);
        } catch (MultipleCompilationErrorsException ex) {
            report.put("status", "error");
            report.put("reason", "fileParseFailed");
            logger.error(ex.getMessage(), ex);
        } catch (Exception ex) {
            report.put("status", "error");
            report.put("reason", "other");
            logger.error(ex.getMessage(), ex);
        } finally {
            FileUtils.deleteQuietly(file);
        }
        importedConnections.put("report", report);
        return importedConnections;
    }

    public Map<String, Object> importConnection(Map<String, Object> map) {
        DatabaseConnectionRegistry databaseConnectionRegistry = getDatabaseConnectionRegistryService((String) map.get("type"));
        if (databaseConnectionRegistry != null) {
            logger.info("Importing connection " + map);
            databaseConnectionRegistry.importConnection(map);
        } else {
            map.put("status", "failed");
            map.put("statusMessage", "invalidDatabaseType");
        }
        return map;
    }

    public String setPassword(Map<String, Object> map, String password) {
        if (password != null && password.contains("_ENC")) {
            password = password.substring(0, 32);
            password = EncryptionUtils.passwordBaseDecrypt(password);
            map.put("password", password);
        } else if (password != null && !password.contains("_ENC")) {
            map.put("password", password);
        }
        return password;
    }

    public void setDslExecutor(DSLExecutor dslExecutor) {
        this.dslExecutor = dslExecutor;
    }

    public void setDslHandlerMap(Map<String, DSLHandler> dslHandlerMap) {
        this.dslHandlerMap = dslHandlerMap;
    }

    public File exportConnections(JSONObject connections) throws JSONException, InstantiationException, IllegalAccessException {
        File file = null;

        try {
            file = File.createTempFile("exportedConnections", ".txt");
            Iterator iterator = connections.keys();
            StringBuilder sb = new StringBuilder();
            while (iterator.hasNext()) {
                String type = (String) iterator.next();
                JSONArray connectionsArray = (JSONArray) connections.get(type);
                for (int i = 0; i < connectionsArray.length(); i++) {
                    String connectionId = connectionsArray.getString(i);
                    sb.append("connection {").append(Utils.NEW_LINE);
                    sb.append(getConnection(connectionId, type).getSerializedExportData());
                    sb.append(Utils.NEW_LINE).append("}").append(Utils.NEW_LINE);
                }
            }
            FileUtils.writeStringToFile(file, sb.toString(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public Map<String, Object> getServerStatus(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException {
        AbstractConnection connection = getConnection(connectionId, databaseType);
        Map<String, Object> serverStatus = new LinkedHashMap<>();
        if (!connection.isConnected()) {
            serverStatus.put("failed", "Connection is disconnected");
            return serverStatus;
        }
        serverStatus.put("success", connection.getServerStatus());
        return serverStatus;

    }

    public Map<String, ConnectorMetaData> getAvailableConnectors() {
        Map<String, ConnectorMetaData> availableConnectors = new LinkedHashMap<>();
        for (DatabaseConnectionRegistry databaseConnectionRegistry : getDatabaseConnectionRegistryServices()) {
            availableConnectors.put(databaseConnectionRegistry.getConnectionType(), databaseConnectionRegistry.getConnectorMetaData());
        }
        return availableConnectors;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    public void setRenderService(RenderService renderService) {
        this.renderService = renderService;
    }

    public void setTemplateManagerService(JahiaTemplateManagerService jahiaTemplateManagerService) {
        this.templateManagerService = jahiaTemplateManagerService;
    }

    public DatabaseConnectionRegistry getDatabaseConnectionRegistryService(String databaseType) {
        try {
            ServiceReference[] serviceReferences = this.bundle.getBundleContext().getAllServiceReferences(DatabaseConnectionRegistry.class.getName(), null);
            if (serviceReferences != null) {
                for (ServiceReference serviceReference : serviceReferences) {
                    DatabaseConnectionRegistry databaseConnectionRegistry = (DatabaseConnectionRegistry) this.bundle.getBundleContext().getService(serviceReference);
                    if (databaseConnectionRegistry.getConnectionType().equals(databaseType)) {
                        return databaseConnectionRegistry;
                    }
                }
            }
        } catch (InvalidSyntaxException ex) {
            logger.error("Could not find service: " + ex.getMessage());
        }
        return null;
    }

    private List<DatabaseConnectionRegistry> getDatabaseConnectionRegistryServices() {
        List<DatabaseConnectionRegistry> databaseConnectionRegistryServices = new LinkedList<>();
        try {
            ServiceReference[] serviceReferences = this.bundle.getBundleContext().getAllServiceReferences(DatabaseConnectionRegistry.class.getName(), null);
            if (serviceReferences != null) {
                for (ServiceReference serviceReference : serviceReferences) {
                    databaseConnectionRegistryServices.add((DatabaseConnectionRegistry) this.bundle.getBundleContext().getService(serviceReference));
                }
            }
        } catch (InvalidSyntaxException ex) {
            logger.error("Could not find service: " + ex.getMessage());
        }
        return databaseConnectionRegistryServices;
    }

    private String resolveDslHandlerType(List<String> superTypes) {
        if (superTypes.contains(DCMIX_DIRECTIVES_DEFINITION)) {
            return "directive";
        } else if (superTypes.contains(DCMIX_SERVICES_DEFINITION)) {
            return "service";
        }
        return null;
    }

    /********************************************************************************************
     * Definition parsing and file aggregation below
     ********************************************************************************************/


    private boolean parseDefinitionWizards(Bundle bundle) throws ParseException, IOException {
        if (org.jahia.osgi.BundleUtils.isJahiaBundle(bundle)) {
            JahiaTemplatesPackage packageById = org.jahia.osgi.BundleUtils.getModule(bundle);
            boolean foundDefinitions = false;
            int parsedDefinitionsCount = 0;
            if (packageById != null) {
                List<String> definitionsFiles = new LinkedList<>(packageById.getDefinitionsFiles());
                for (String definitionsFile : definitionsFiles) {
                    logger.debug("\tRetrieving Bundle resource...");
                    BundleResource bundleResource = new BundleResource(bundle.getResource(definitionsFile), bundle);
                    List<ExtendedNodeType> definitionsFromFile = NodeTypeRegistry.getInstance().getDefinitionsFromFile(bundleResource, bundle.getSymbolicName());
                    for (ExtendedNodeType type : definitionsFromFile) {
                        if (!type.isMixin()) {
                            String definitionType = resolveDslHandlerType(Arrays.asList(type.getDeclaredSupertypeNames()));
                            if (definitionType != null) {
                                String url = type.getName().replace(":", "_") + "/html/" + StringUtils.substringAfter(type.getName(), ":") + ".wzd";
                                logger.debug("\tRetrieving definition wzd resource for: " + type.getName() + "(" + url + ")");
                                URL resource = bundle.getResource(type.getName().replace(":", "_") + "/html/" + StringUtils.substringAfter(type.getName(), ":") + ".wzd");
                                if (resource != null) {
                                        logger.info("\tPreparing to execute DSL handler to register " + definitionType);
                                        foundDefinitions = true;
                                        dslExecutor.execute(resource, dslHandlerMap.get(definitionType), packageById, type);
                                        parsedDefinitionsCount++;
                                } else {
                                    logger.warn("\tCould not locate resource for definition: " + type.getName());
                                }
                            } else {
                                logger.debug("\tSkipping definition: " + type + " - super types is not of: " + DCMIX_DIRECTIVES_DEFINITION +  " or " + DCMIX_SERVICES_DEFINITION);
                            }
                        }
                    }
                }
            } else {
                logger.debug("\tNo Jahia Template Package found for bundle [" + bundle.getSymbolicName() + "]");
            }
            if(parsedDefinitionsCount>0) {
                logger.info("\tRegistered: (" + parsedDefinitionsCount + ") definitions. Parsing completed successfully!");
            }
            return foundDefinitions;
        }  else {
            logger.debug("\t[" + bundle.getSymbolicName() + "] is not a Jahia bundle. Skipping...");
        }
        return false;
    }

    /**
     * This function get the path of the file which handle the directives use by angular
     *
     * @param renderContext The render context on which to get the current site
     * @return The path of the file to use
     * @throws Exception
     */
    public String getAngularConfigFilePath(RenderContext renderContext) throws Exception {
        String key = getCacheKey(renderContext);

        if (settingsBean.isDevelopmentMode()) {
            Semaphore semaphore = processings.get(key);
            if (semaphore == null) {
                semaphore = new Semaphore(1);
                Semaphore semaphoreOld = processings.putIfAbsent(key, semaphore);
                if(semaphoreOld != null) {
                    semaphore = semaphoreOld;
                }
            }
            try {
                semaphore.acquire();
                if (!semaphore.hasQueuedThreads()) {
                    angularConfigFilesPath.remove(key);
                }
                if (angularConfigFilesPath.get(key) != null && new File(angularConfigFilesPath.get(key)).exists())
                    return angularConfigFilesPath.get(key);
                return getAngularConfigFilePath(renderContext, key);
            } catch (InterruptedException e) {
                logger.debug(e.getMessage(), e);
                Thread.currentThread().interrupt();
                throw e;
            } finally {
                semaphore.release();
            }
        }

        //Make sure the file actually exists
        if (angularConfigFilesPath.get(key) == null || !new File(angularConfigFilesPath.get(key)).exists()) {
            Semaphore semaphore = processings.get(key);
            if (semaphore == null) {
                semaphore = new Semaphore(1);
                Semaphore semaphoreOld = processings.putIfAbsent(key, semaphore);
                if(semaphoreOld != null) {
                    semaphore = semaphoreOld;
                }
            }
            try {
                semaphore.acquire();
                if (angularConfigFilesPath.get(key) != null && new File(angularConfigFilesPath.get(key)).exists())
                    return angularConfigFilesPath.get(key);
                logger.info("Generating js file for Database Connector for key {}", key);
                return getAngularConfigFilePath(renderContext, key);
            } catch (InterruptedException e) {
                logger.debug(e.getMessage(), e);
                Thread.currentThread().interrupt();
                throw e;
            } finally {
                semaphore.release();
            }
        } else {
            return angularConfigFilesPath.get(key);
        }
    }

    public Long getAngularConfigFileTimestamp(RenderContext renderContext) throws Exception {
        String key = getCacheKey(renderContext);
        if (settingsBean.isDevelopmentMode()) {
            if (renderContext.getRequest().getAttribute("dbcdevconfigfiletimestamp") == null) {
                long time;
                if (lastBundleEvent.after(lastDeployDate)) {
                    time = lastBundleEvent.getTime();
                } else {
                    time = lastDeployDate.getTime();
                }
                renderContext.getRequest().setAttribute("dbcdevconfigfiletimestamp", String.valueOf(time));
            }
            return Long.valueOf((String) renderContext.getRequest().getAttribute("dbcdevconfigfiletimestamp"));
        } else {
            return angularConfigFilesTimestamp.get(key);
        }
    }

    private String getCacheKey(RenderContext renderContext) {
        String language = renderContext.getUILocale().toString();
        String mode = "/builder";
        return renderContext.getSiteInfo().getSitePath() + renderContext.getWorkspace() + mode + "/" + language;
    }

    private String getAngularConfigFilePath(RenderContext renderContext, String key) throws RepositoryException, IOException {
        String fileName = "database-connector-angular-builder-config_" + renderContext.getSite().getIdentifier() + "_" + renderContext.getUILocale().toString() + ".js.temp";
        String filePath = prepareAngularConfigFile(renderContext, fileName);
        Set<String> extraResourceBundles = new HashSet<>();

        addJSToAngularConfigFile(renderContext, DEFINITION_QUERY, filePath, extraResourceBundles);
        addJSToAngularConfigFileByViewName(renderContext, SERVICES_QUERY, SERVICE_VIEW_NAME, filePath, extraResourceBundles);
        for (String extraResourceBundle : extraResourceBundles) {
            rbExecutor.addRBDictionnaryToAngularConfigFile(userManagerService.lookupRootUser().getJahiaUser(), renderContext.getUILocale(), filePath, templateManagerService.getTemplatePackageById(extraResourceBundle));
        }
        addConnectorAvailability(filePath);
        File file = new File(filePath);

        if (FileUtils.sizeOf(file) == 0) {
            FileUtils.forceDelete(file);
            return null;
        } else {
            filePath = file.getAbsolutePath().replace(".temp", "");
            file.renameTo(new File(filePath));

            angularConfigFilesPath.put(key, filePath);
            angularConfigFilesTimestamp.put(key, file.lastModified());
            return filePath;
        }
    }

    /**
     * This function generate the javascript file to contains directives use by angular
     *
     * @param renderContext               The render context on which to get the current site
     * @param extraResourceBundlePackages
     * @return The path to the generated file
     * @throws RepositoryException
     */
    private void addJSToAngularConfigFile(final RenderContext renderContext, final String query, final String filePath, final Set<String> extraResourceBundlePackages) throws RepositoryException, IOException {
        jcrTemplate.doExecuteWithSystemSessionAsUser(userManagerService.lookupRootUser().getJahiaUser(), Constants.EDIT_WORKSPACE, renderContext.getUILocale(), new JCRCallback<Object>() {
            @Override
            public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                List<JahiaTemplatesPackage> dependencies = getDependentModules();
                File jsFile = new File(filePath);
                try {
                    FileWriter fw = new FileWriter(jsFile.getAbsoluteFile(), true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    for (JahiaTemplatesPackage pack : dependencies) {
                        try {
                            QueryManager qm = session.getWorkspace().getQueryManager();
                            String rootPath = pack.getRootFolderPath() + "/" + pack.getVersion().toString();
                            Query q = qm.createQuery(MessageFormat.format(query, rootPath), Query.JCR_SQL2);
                            NodeIterator ni = q.execute().getNodes();

                            while (ni.hasNext()) {
                                extraResourceBundlePackages.add(pack.getId());
                                JCRNodeWrapper node = (JCRNodeWrapper) ni.next();
                                String[] views = getDirectiveViews(node);
                                for (String view : views) {
                                    writeViewToWriter(node, renderContext, bw, view);
                                }
                            }
                        } catch (RenderException e) {
                            logger.error("Failed to render view: " + e.getMessage() + "\n" + e);
                        }
                    }
                    bw.close();
                } catch (IOException e) {
                    logger.error("Failed to write to buffer: " + e.getMessage() + "\n" + e);
                }
                return null;
            }
        });
    }

    /**
     * This function generate the javascript file to contains directives used by angular using a view name
     *
     * @param renderContext               The render context on which to get the current site
     * @param extraResourceBundlePackages
     * @return The path to the generated file
     * @throws RepositoryException
     */
    private void addJSToAngularConfigFileByViewName(final RenderContext renderContext, final String query, final String viewName, final String filePath, final Set<String> extraResourceBundlePackages) throws RepositoryException, IOException {
        jcrTemplate.doExecuteWithSystemSessionAsUser(userManagerService.lookupRootUser().getJahiaUser(), Constants.EDIT_WORKSPACE, renderContext.getUILocale(), new JCRCallback<Object>() {
            @Override
            public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                List<JahiaTemplatesPackage> dependencies = getDependentModules();
                File jsFile = new File(filePath);
                try {
                    FileWriter fw = new FileWriter(jsFile.getAbsoluteFile(), true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    for (JahiaTemplatesPackage pack : dependencies) {
                        try {
                            if (pack != null) {
                                QueryManager qm = session.getWorkspace().getQueryManager();
                                String rootPath = pack.getRootFolderPath() + "/" + pack.getVersion().toString();
                                Query q = qm.createQuery(MessageFormat.format(query, rootPath), Query.JCR_SQL2);
                                NodeIterator ni = q.execute().getNodes();

                                while (ni.hasNext()) {
                                    extraResourceBundlePackages.add(pack.getId());
                                    writeViewToWriter((JCRNodeWrapper) ni.next(), renderContext, bw, viewName);
                                }
                            }
                        } catch (RenderException e) {
                            logger.error("Failed to render view: " + e.getMessage() + "\n" + e);
                        }
                    }
                    bw.close();
                } catch (IOException e) {
                    logger.error("Failed to write to buffer: " + e.getMessage() + "\n" + e);
                }
                return null;
            }
        });
    }

    private String[] getDirectiveViews(JCRNodeWrapper node) throws RepositoryException {
        List<String> views = new ArrayList<>();
        views.add("connectionDirective");
        JCRNodeWrapper statusDirectives = node.getNode("statusDirectives");
        List<JCRNodeWrapper> sd = JCRContentUtils.getChildrenOfType(statusDirectives, "dc:directiveDefinition");
        for (JCRNodeWrapper d : sd) {
            views.add(d.getPropertyAsString("name"));
        }
        return views.toArray(new String[0]);
    }

    private List<JahiaTemplatesPackage> getDependentModules() {
        List<JahiaTemplatesPackage> packages = templateManagerService.getAvailableTemplatePackages();
        JahiaTemplatesPackage currentPackage = templateManagerService.getTemplatePackageById("database-connector");
        List<JahiaTemplatesPackage> dependents = new ArrayList<>();
        for (JahiaTemplatesPackage pack : packages) {
            if (pack.getDependencies().contains(currentPackage)) {
                dependents.add(pack);
            }
        }
        return dependents;
    }

    /**
     * Renders and writes a given view, if it exists, to write buffer (bw).
     *
     * @param currentNode
     * @param renderContext
     * @param bw
     * @param viewName
     * @throws IOException
     * @throws RenderException
     * @throws RepositoryException
     */
    private void writeViewToWriter(JCRNodeWrapper currentNode, RenderContext renderContext, BufferedWriter bw, String viewName) throws IOException, RenderException, RepositoryException {
        if (renderService.hasView(currentNode, viewName, "js", renderContext)) {
            bw.write("/**\n");
            bw.write(" * nodeType: " + currentNode.getPrimaryNodeTypeName() + "\n");
            bw.write(" * path: " + currentNode.getPath() + "\n");
            bw.write(" */\n");
            String render = renderService.render(new Resource(currentNode, "js", viewName, Resource.CONFIGURATION_INCLUDE), renderContext);
            bw.write(render);
            bw.newLine();
        }
    }

    private String prepareAngularConfigFile(RenderContext renderContext, final String fileName) throws RepositoryException {
        return jcrTemplate.doExecuteWithSystemSessionAsUser(userManagerService.lookupRootUser().getJahiaUser(), Constants.EDIT_WORKSPACE, renderContext.getUILocale(), new JCRCallback<String>() {
            @Override
            public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                new File(getFileSystemPath("/generated-resources")).mkdirs();

                File jsFile = new File(getFileSystemPath("/generated-resources/" + fileName));
                try {
                    if (!jsFile.exists()) {
                        jsFile.createNewFile();
                    } else if (FileUtils.isFileOlder(jsFile, lastDeployDate) || FileUtils.isFileOlder(jsFile, lastBundleEvent)) {
                        FileUtils.forceDelete(jsFile);
                        jsFile.createNewFile();
                    }
                    FileWriter fw = new FileWriter(jsFile.getAbsoluteFile());
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write("");
                    bw.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
                return jsFile.getPath();
            }
        });
    }

    private String getFileSystemPath(String path) {
        try {
            return SettingsBean.class.getMethod("getJahiaGeneratedResourcesDiskPath", null).invoke(SettingsBean.getInstance()) + path.replace("/generated-resources", "");
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("Failed to get file path for generated resources " + e.getMessage() + "\n" + e);
            return SettingsBean.getInstance().getJahiaVarDiskPath() + path;
        } catch (NoSuchMethodException e) {
            logger.error("Older version of SettingsBean detected " + e.getMessage() + "\n" + e);
            return SettingsBean.getInstance().getJahiaVarDiskPath() + path;
        }
    }

    private void addConnectorAvailability(String filePath) {
        File jsFile = new File(filePath);
        try {
            FileWriter fw = new FileWriter(jsFile.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.newLine();
            bw.write("(function(){\n");
            bw.write("angular.module('databaseConnector').config(function(contextualData) {\n");
            bw.write("contextualData.resolvedConnectors = [];\n");
            for (DatabaseConnectionRegistry databaseConnectionRegistry : getDatabaseConnectionRegistryServices()) {
                bw.write("contextualData.resolvedConnectors.push('" + databaseConnectionRegistry.getConnectionType() + "');\n");
            }
            bw.write("});\n");
            bw.write("})();");

            bw.close();
        } catch (IOException e) {
            logger.error("Failed to write to buffer: " + e.getMessage() + "\n" + e);
        }
    }
}
