package org.jahia.modules.databaseConnector.connection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.modules.databaseConnector.factories.DatabaseConnectionRegistryFactory;
import org.jahia.modules.databaseConnector.util.Utils;
import org.jahia.modules.databaseConnector.dsl.DSLExecutor;
import org.jahia.modules.databaseConnector.dsl.DSLHandler;
import org.jahia.osgi.BundleResource;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ParseException;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.EncryptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Date: 2013-10-17
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
@Component(service = DatabaseConnectorManager.class)
public class DatabaseConnectorManager implements InitializingBean, BundleListener {

    public static final String DATABASE_CONNECTOR_ROOT_PATH = "/settings/";

    public static final String DATABASE_CONNECTOR_PATH = "databaseConnector";

    public static final String DATABASE_CONNECTOR_NODE_TYPE = "dc:databaseConnector";

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectorManager.class);
    private static DatabaseConnectorManager instance;
    private BundleContext context;
    private DSLExecutor dslExecutor;
    private Map<String, DSLHandler> dslHandlerMap;
    private Map<String, DatabaseConnectionRegistry> databaseConnectionRegistries;
    private Map<String, String> availableDatabaseTypes = new LinkedHashMap<>();
    private final static Set<Long> installedBundles = new LinkedHashSet<>();
    private SettingsBean settingsBean;

    @Activate
    public void activate(BundleContext context) {
        this.context = context;
        this.context.addBundleListener(this);
        databaseConnectionRegistries = new TreeMap<>();
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

    public <T extends AbstractConnection> Map<String, T> getConnections(String databaseType) throws InstantiationException, IllegalAccessException {
        return findRegisteredConnections().get(databaseType);
    }

    public <T extends AbstractConnection, E extends AbstractDatabaseConnectionRegistry> Map<String, Map> findRegisteredConnections() throws InstantiationException, IllegalAccessException{
        Map<String, Map> registeredConnections = new HashMap<>();
        for (Map.Entry<String, DatabaseConnectionRegistry> entry: DatabaseConnectionRegistryFactory.getRegisteredConnections().entrySet()) {
            String connectionType = entry.getKey();
            Map<String, T> registry = databaseConnectionRegistries.get(connectionType).getRegistry();
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
        return databaseConnectionRegistries.get(connection.getDatabaseType()).addEditConnection(connection, isEdition);
    }

    public boolean removeConnection(String connectionId, String databaseType) {
        return databaseConnectionRegistries.get(databaseType).removeConnection(connectionId);
    }

    public boolean updateConnection(String connectionId, String databaseType, boolean connect) {
        if (connect) {
            if (((AbstractConnection) databaseConnectionRegistries.get(databaseType).getRegistry().get((connectionId))).testConnectionCreation()) {
                databaseConnectionRegistries.get(databaseType).connect(connectionId);
            } else {
                return false;
            }
        } else {
            databaseConnectionRegistries.get(databaseType).disconnect(connectionId);
        }
        return true;
    }

    public boolean testConnection(AbstractConnection connection) {
        return databaseConnectionRegistries.get(connection.getDatabaseType()).testConnection(connection);
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
            for (Map.Entry<String, String> entry: this.availableDatabaseTypes.entrySet()) {
                String databaseType = entry.getKey();
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
        logger.info("Importing connection " + map);
        if (databaseConnectionRegistries.containsKey(map.get("type"))) {
            databaseConnectionRegistries.get(map.get("type")).importConnection(map);
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

    public Map<String, String> getAvailableDatabaseTypes() {
        return availableDatabaseTypes;
    }

    public void registerConnectorToRegistry(String connectionType, DatabaseConnectionRegistry databaseConnectionRegistry) {
        DatabaseConnectionRegistryFactory.registerConnectionType(connectionType, databaseConnectionRegistry);
        try {
            DatabaseConnectionRegistryFactory.makeDatabaseConnectionRegistry(databaseConnectionRegistry);
            databaseConnectionRegistries.put(connectionType, databaseConnectionRegistry);
            Map registry = databaseConnectionRegistry.getRegistry();
            Set<String> set = registry.keySet();
            for (String connectionId : set) {
                //Only register the service if it was previously connected and registered.
                if (((AbstractConnection) registry.get(connectionId)).isConnected()) {
                    ((AbstractConnection) registry.get(connectionId)).registerAsService();
                }
            }
            availableDatabaseTypes.put(databaseConnectionRegistry.getConnectionType(), databaseConnectionRegistry.getConnectionDisplayName());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public <T extends AbstractConnection> void deregisterConnectorFromRegistry(String connectionType) {
        DatabaseConnectionRegistry databaseConnectionRegistry = databaseConnectionRegistries.get(connectionType);
        databaseConnectionRegistry.beforeRegistryRemoval();
        Map<String, T> registry = databaseConnectionRegistries.get(connectionType).getRegistry();
        for (Map.Entry<String, T> entry: registry.entrySet()) {
            T connection = entry.getValue();
            if (connection.isConnected()) {
                connection.unregisterAsService();
            }
        }
        databaseConnectionRegistries.remove(connectionType);
    }

    public BundleContext getBundleContext() {
        return this.context;
    }

    public DatabaseConnectionRegistry getConnectionRegistryClassInstance(String databaseType) {
        return databaseConnectionRegistries.get(databaseType);
    }

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
                            if (superTypes.contains("dcmix:directiveDefinition")) {
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

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }
}
