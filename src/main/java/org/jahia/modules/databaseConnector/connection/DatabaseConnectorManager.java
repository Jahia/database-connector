package org.jahia.modules.databaseConnector.connection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.modules.databaseConnector.connector.AbstractConnectorMetaData;
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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
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

/**
 * Date: 2013-10-17
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
@Component(service = DatabaseConnectorManager.class, immediate = true)
public class DatabaseConnectorManager implements InitializingBean, BundleListener {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectorManager.class);

    public static final String DATABASE_CONNECTOR_ROOT_PATH = "/settings/";
    public static final String DATABASE_CONNECTOR_PATH = "databaseConnector";
    public static final String DATABASE_CONNECTOR_NODE_TYPE = "dc:databaseConnector";

    private static final String DEFINITION_QUERY = "SELECT * FROM [dcmix:directivesDefinition] AS result WHERE ISDESCENDANTNODE(result, ''{0}'')";

    private final static Object lock = new Object();
    private static DatabaseConnectorManager instance;
    private BundleContext context;
    private DSLExecutor dslExecutor;
    private Map<String, DSLHandler> dslHandlerMap;
    private Map<String, AbstractConnectorMetaData> availableConnectors = new LinkedHashMap<>();
    private final static Set<Long> installedBundles = new LinkedHashSet<>();
    private final static Map<String, String> angularConfigFilesPath = new HashMap<>();
    private final static Map<String, Long> angularConfigFilesTimestamp = new HashMap<>();
    private SettingsBean settingsBean;
    private JCRTemplate jcrTemplate;
    private RenderService renderService;
    private JahiaTemplateManagerService templateManagerService;
    private Bundle bundle;

    private static Date lastDeployDate;

    @Activate
    public void activate(BundleContext context) {
        this.context = context;
        this.context.addBundleListener(this);
        this.bundle = this.context.getBundle();
        instance = this;
    }

    public static DatabaseConnectorManager getInstance() {
        return instance;
    }

    @Override
    public void bundleChanged(BundleEvent bundleEvent) {
        if (settingsBean.isProcessingServer()) {
            Bundle bundleEventBundle = bundleEvent.getBundle();
//            if (bundleEvent.getType() == BundleEvent.STARTED || bundleEvent.getType() == BundleEvent.STOPPED) {
//                FLush stuff here
//            }
            long bundleId = bundleEvent.getBundle().getBundleId();
            if (bundleEvent.getType() == BundleEvent.INSTALLED || bundleEvent.getType() == BundleEvent.UPDATED) {
                installedBundles.add(bundleId);
            }
            if ((bundleEvent.getType() == BundleEvent.RESOLVED && installedBundles.contains(bundleId)) || (bundleEventBundle.getState() == Bundle.RESOLVED && bundleEvent.getType() == BundleEvent.INSTALLED)) {
                installedBundles.remove(bundleId);
                try {
                    logger.info("Parsing directive definitions");
                    parseDefinitionWizards(bundleEvent.getBundle());
                } catch (ParseException e) {
                    logger.error("Parse exception: " + e.getMessage());
                } catch (IOException e) {
                    logger.error("IO exception: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (settingsBean.isProcessingServer()) {
            lastDeployDate = new Date();
            parseDefinitionWizards(bundle);
            for (Bundle currentBundle : bundle.getBundleContext().getBundles()) {
                if (!(currentBundle.getSymbolicName().equals(bundle.getSymbolicName()))
                        && org.jahia.osgi.BundleUtils.isJahiaModuleBundle(currentBundle)
                        && (currentBundle.getState() == Bundle.INSTALLED
                            || currentBundle.getState() == Bundle.RESOLVED
                            || currentBundle.getState() == Bundle.ACTIVE)) {
                    parseDefinitionWizards(currentBundle);
                }
            }
        }
    }

    public <T extends AbstractConnection> Map<String, T> getConnections(String databaseType) throws InstantiationException, IllegalAccessException {
        return findRegisteredConnections().get(databaseType);
    }

    public <T extends AbstractConnection, E extends AbstractDatabaseConnectionRegistry> Map<String, Map> findRegisteredConnections() throws InstantiationException, IllegalAccessException{
        Map<String, Map> registeredConnections = new HashMap<>();
        for (Map.Entry<String, AbstractConnectorMetaData> entry: availableConnectors.entrySet()) {
            DatabaseConnectionRegistry databaseConnectionRegistry = (DatabaseConnectionRegistry) Utils.getService(entry.getValue().getRegistryClassName(), this.context);
            String connectionType = entry.getKey();
            Map<String, T> registry = databaseConnectionRegistry.getRegistry();
            if (!registry.isEmpty()) {
                Map<String, T> connectionSet = new HashMap<>();
                for (Map.Entry<String, T> registeryEntry : registry.entrySet()) {
                    connectionSet.put(registeryEntry.getKey(), registeryEntry.getValue());
                }
                registeredConnections.put(connectionType, connectionSet);
            }
        }
        return registeredConnections;
    }

    public <T extends AbstractConnection> T getConnection(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException{
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
        AbstractConnectorMetaData abstractConnectorMetaData = availableConnectors.get(connection.getDatabaseType());
        DatabaseConnectionRegistry databaseConnectionRegistry = (DatabaseConnectionRegistry) Utils.getService(abstractConnectorMetaData.getRegistryClassName(), this.context);
        return databaseConnectionRegistry.addEditConnection(connection, isEdition);
    }

    public boolean removeConnection(String connectionId, String databaseType) {
        AbstractConnectorMetaData abstractConnectorMetaData = availableConnectors.get(databaseType);
        DatabaseConnectionRegistry databaseConnectionRegistry = (DatabaseConnectionRegistry) Utils.getService(abstractConnectorMetaData.getRegistryClassName(), this.context);
        return databaseConnectionRegistry.removeConnection(connectionId);
    }

    public boolean updateConnection(String connectionId, String databaseType, boolean connect) {
        AbstractConnectorMetaData abstractConnectorMetaData = availableConnectors.get(databaseType);
        DatabaseConnectionRegistry databaseConnectionRegistry = (DatabaseConnectionRegistry) Utils.getService(abstractConnectorMetaData.getRegistryClassName(), this.context);
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
        AbstractConnectorMetaData abstractConnectorMetaData = availableConnectors.get(connection.getDatabaseType());
        DatabaseConnectionRegistry databaseConnectionRegistry = (DatabaseConnectionRegistry) Utils.getService(abstractConnectorMetaData.getRegistryClassName(), this.context);
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
            for (Map.Entry<String, AbstractConnectorMetaData> entry: this.availableConnectors.entrySet()) {
                String databaseType = entry.getValue().getDatabaseType();
                if (parsedConnections.containsKey(databaseType)) {
                    Map<String, List> results = new LinkedHashMap<>();
                    List<Map> validConnections = new LinkedList();
                    List<Map> failedConnections = new LinkedList();
                    for (Map connectionConfiguration: (LinkedList<Map>)parsedConnections.get(databaseType)) {
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
        if (availableConnectors.containsKey(map.get("type"))) {
            logger.info("Importing connection " + map);
            AbstractConnectorMetaData abstractConnectorMetaData = availableConnectors.get(map.get("type"));
            DatabaseConnectionRegistry databaseConnectionRegistry = (DatabaseConnectionRegistry) Utils.getService(abstractConnectorMetaData.getRegistryClassName(), this.context);
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

    public File exportConnections(JSONObject connections) throws JSONException, InstantiationException, IllegalAccessException{
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

    public Map<String, Object> getServerStatus(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException{
        AbstractConnection connection = getConnection(connectionId, databaseType);
        Map<String, Object> serverStatus = new LinkedHashMap<>();
        if (!connection.isConnected()) {
            serverStatus.put("failed", "Connection is disconnected");
            return serverStatus;
        }
        serverStatus.put("success", connection.getServerStatus());
        return serverStatus;

    }

    public Map<String, AbstractConnectorMetaData> getAvailableConnectors() {
        return availableConnectors;
    }

    public void registerConnectorToRegistry(String connectionType, AbstractConnectorMetaData connectorMetaData) {
        try {
            availableConnectors.put(connectionType, connectorMetaData);
            //Get the service that is registering to databse connector and retrieve the registry.
            ServiceReference databaseConnectionRegisteryRef = this.context.getServiceReference(connectorMetaData.getRegistryClassName());
            DatabaseConnectionRegistry databaseConnectionRegistry = (DatabaseConnectionRegistry) this.context.getService(databaseConnectionRegisteryRef);
            Map registry = databaseConnectionRegistry.getRegistry();
            Set<String> set = registry.keySet();
            for (String connectionId : set) {
                //Only register the service if it was previously connected and registered.
                if (((AbstractConnection) registry.get(connectionId)).isConnected()) {
                    ((AbstractConnection) registry.get(connectionId)).registerAsService();
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public <T extends AbstractConnection> void deregisterConnectorFromRegistry(String connectionType) {
        AbstractConnectorMetaData abstractConnectorMetaData = availableConnectors.get(connectionType);
        DatabaseConnectionRegistry databaseConnectionRegistry = (DatabaseConnectionRegistry) Utils.getService(abstractConnectorMetaData.getRegistryClassName(), this.context);
        databaseConnectionRegistry.beforeRegistryRemoval();
        Map<String, T> registry = databaseConnectionRegistry.getRegistry();
        for (Map.Entry<String, T> entry: registry.entrySet()) {
            T connection = entry.getValue();
            if (connection.isConnected()) {
                connection.unregisterAsService();
            }
        }
        availableConnectors.remove(connectionType);
    }

    public BundleContext getBundleContext() {
        return this.context;
    }

//    public DatabaseConnectionRegistry getConnectionRegistryClassInstance(String databaseType) {
//        AbstractConnectorMetaData abstractConnectorMetaData = availableConnectors.get(databaseType);
//        DatabaseConnectionRegistry databaseConnectionRegistry = (DatabaseConnectionRegistry) Utils.getService(abstractConnectorMetaData.getRegistryClassName(), this.context);
//        return databaseConnectionRegistries.get(databaseType);
//    }

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

    /********************************************************************************************
     * Definition parsing and file aggregation below
     ********************************************************************************************/


    private boolean parseDefinitionWizards(Bundle bundle) throws ParseException, IOException {
        JahiaTemplatesPackage packageById = org.jahia.osgi.BundleUtils.getModule(bundle);
        boolean foundDefinitions = false;
        if (packageById != null) {
            List<String> definitionsFiles = new LinkedList<>(packageById.getDefinitionsFiles());
            for (String definitionsFile : definitionsFiles) {
                BundleResource bundleResource = new BundleResource(bundle.getResource(definitionsFile), bundle);
                List<ExtendedNodeType> definitionsFromFile = NodeTypeRegistry.getInstance().getDefinitionsFromFile(bundleResource, bundle.getSymbolicName());
                for (ExtendedNodeType type : definitionsFromFile) {
                    if (!type.isMixin()) {
                        List<String> superTypes = Arrays.asList(type.getDeclaredSupertypeNames());

                        URL resource = bundle.getResource(type.getName().replace(":", "_") + "/html/" + StringUtils.substringAfter(type.getName(), ":") + ".wzd");
                        if (resource != null) {
                            if (superTypes.contains("dcmix:directivesDefinition")) {
                                foundDefinitions = true;
                                dslExecutor.execute(resource, dslHandlerMap.get("directive"), packageById, type);
                            }
                        }
                    }
                }
            }
        }
        return foundDefinitions;
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
            return getAngularConfigFilePath(renderContext, key);
        }

        if (angularConfigFilesPath.get(key) == null) {
            synchronized (lock) {
                if (angularConfigFilesPath.get(key) != null) return angularConfigFilesPath.get(key);
                return getAngularConfigFilePath(renderContext, key);
            }
        } else {
            return angularConfigFilesPath.get(key);
        }
    }

    public Long getAngularConfigFileTimestamp(RenderContext renderContext) throws Exception {
        String key = getCacheKey(renderContext);
        if (settingsBean.isDevelopmentMode()) {
            if(renderContext.getRequest().getAttribute("ffdevconfigfiletimestamp") == null) {
                renderContext.getRequest().setAttribute("ffdevconfigfiletimestamp", String.valueOf(System.currentTimeMillis()));
            }
            return Long.valueOf((String) renderContext.getRequest().getAttribute("ffdevconfigfiletimestamp"));
        }
        else if (angularConfigFilesTimestamp.containsKey(key)) return angularConfigFilesTimestamp.get(key);
        else return System.currentTimeMillis();
    }

    private String getCacheKey(RenderContext renderContext) {
        String language = renderContext.getUILocale().toString();
        String mode = "/builder";
        return renderContext.getSiteInfo().getSitePath() + renderContext.getWorkspace() + mode + "/" + language;
    }

    private String getAngularConfigFilePath(RenderContext renderContext, String key) throws RepositoryException, IOException {
        String fileName = "database-connector-angular-builder-config_" + renderContext.getSite().getIdentifier() + "_" + renderContext.getUILocale().toString() + ".js";
        String filePath = prepareAngularConfigFile(renderContext, fileName);
        Set<String> extraResourceBundles = new HashSet<>();

        addJSToAngularConfigFile(renderContext, DEFINITION_QUERY, filePath, extraResourceBundles);

        //TODO handle resource bundles
        // Find Resource Bundle for theme
//        ExtendedNodeType nodeType = NodeTypeRegistry.getInstance().getNodeType("fcnt:form");
//        for (ScriptResolver scriptResolver : renderService.getScriptResolvers()) {
//            SortedSet<View> viewsSet = scriptResolver.getViewsSet(nodeType, renderContext.getSite(), "html");
//            for (View view : viewsSet) {
//                String displayName = view.getDisplayName();
//                if (displayName.startsWith("form.")) {
//                    String id = view.getModule().getId();
//                    if (!extraResourceBundles.contains(id) && !"form-factory-core".equals(id)) {
//                        extraResourceBundles.add(id);
//                    }
//                }
//            }
//        }
//        for (String extraResourceBundle : extraResourceBundles) {
//            addRBDictionnaryToAngularConfigFile(renderContext, filePath, extraResourceBundle);
//        }

        File file = new File(filePath);
        if (FileUtils.sizeOf(file) == 0 || !FileUtils.isFileNewer(file, lastDeployDate)) {
            FileUtils.forceDelete(file);
        } else {
            angularConfigFilesPath.put(key, filePath);
            angularConfigFilesTimestamp.put(key, file.lastModified());
        }
        return filePath;
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
        jcrTemplate.doExecuteWithSystemSessionAsUser(JahiaUserManagerService.getInstance().lookupRootUser().getJahiaUser(), Constants.EDIT_WORKSPACE, renderContext.getUILocale(), new JCRCallback<Object>() {
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
//                                String[] views = node.getPropertyAsString("views").split(" ");
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
        return jcrTemplate.doExecuteWithSystemSessionAsUser(JahiaUserManagerService.getInstance().lookupRootUser().getJahiaUser(), Constants.EDIT_WORKSPACE, renderContext.getUILocale(), new JCRCallback<String>() {
            @Override
            public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                new File(getFileSystemPath("/generated-resources")).mkdirs();

                File jsFile = new File(getFileSystemPath("/generated-resources/" + fileName));
                try {
                    if (!jsFile.exists())
                        jsFile.createNewFile();

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
}
