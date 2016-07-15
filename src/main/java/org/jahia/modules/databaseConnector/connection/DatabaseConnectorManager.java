package org.jahia.modules.databaseConnector.connection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.jahia.modules.databaseConnector.Utils;
import org.jahia.modules.databaseConnector.connection.mongo.MongoConnection;
import org.jahia.modules.databaseConnector.connection.redis.RedisConnection;
import org.jahia.modules.databaseConnector.dsl.DSLExecutor;
import org.jahia.modules.databaseConnector.dsl.DSLHandler;
import org.jahia.utils.EncryptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.jahia.modules.databaseConnector.connection.DatabaseTypes.getAllDatabaseTypes;

/**
 * Date: 2013-10-17
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class DatabaseConnectorManager implements BundleContextAware, InitializingBean {
    
    public static final String DATABASE_CONNECTOR_ROOT_PATH = "/settings/";

    public static final String DATABASE_CONNECTOR_PATH = "databaseConnector";

    public static final String DATABASE_CONNECTOR_NODE_TYPE = "dc:databaseConnector";
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectorManager.class);
    private static DatabaseConnectorManager instance;
    private BundleContext bundleContext;
    private DSLExecutor dslExecutor;
    private Map<String,DSLHandler> dslHandlerMap;
    private Map<DatabaseTypes, DatabaseConnectionRegistry> databaseConnectionRegistries;

    private Set<DatabaseTypes> activatedDatabaseTypes = getAllDatabaseTypes();

    public DatabaseConnectorManager() {
        databaseConnectionRegistries = new TreeMap<DatabaseTypes, DatabaseConnectionRegistry>();
    }

    public static DatabaseConnectorManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectorManager.class) {
                if (instance == null) {
                    instance = new DatabaseConnectorManager();
                }
            }
        }
        return instance;
    }

    @Override
    public void afterPropertiesSet() {
        for (DatabaseTypes activatedDatabaseType : activatedDatabaseTypes) {
            try {
                DatabaseConnectionRegistry databaseConnectionRegistry = DatabaseConnectionRegistryFactory.makeDatabaseConnectionRegistry(activatedDatabaseType);
                databaseConnectionRegistries.put(activatedDatabaseType, databaseConnectionRegistry);
                Map registry = databaseConnectionRegistry.getRegistry();
                Set set = registry.keySet();
                for (Object connectionId : set) {
                    //Only register the service if it was previously connected and registered.
                    if (((AbstractConnection) registry.get(connectionId)).isConnected()) {
                        ((AbstractConnection) registry.get(connectionId)).registerAsService();
                    }
                }
            } catch(Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public <T extends AbstractConnection> Map<String, T> getRegisteredConnections (DatabaseTypes databaseType) {
        return findRegisteredConnections().get(databaseType);
    }

    public Map<DatabaseTypes, Map> findRegisteredConnections() {
        Map<DatabaseTypes, Map> registeredConnections = new HashMap<>();
        for (DatabaseTypes databaseType : databaseConnectionRegistries.keySet()) {
            switch (databaseType) {
                case MONGO:
                    Map<String, MongoConnection> mongoRegistry = databaseConnectionRegistries.get(databaseType).getRegistry();
                    if (!mongoRegistry.isEmpty()) {
                        Map<String, MongoConnection> mongoConnectionSet = new HashMap<>();
                        for (Map.Entry<String, MongoConnection> entry : mongoRegistry.entrySet()) {
                            mongoConnectionSet.put(entry.getKey(), entry.getValue());
                        }
                        registeredConnections.put(DatabaseTypes.MONGO, mongoConnectionSet);
                    }
                    break;
                case REDIS:
                    Map<String, RedisConnection> redisRegistry = databaseConnectionRegistries.get(databaseType).getRegistry();
                    if (!redisRegistry.isEmpty()) {
                        Map<String, RedisConnection> redisConnectionSet = new HashMap<>();
                        for (Map.Entry<String, RedisConnection> entry : redisRegistry.entrySet()) {
                            redisConnectionSet.put(entry.getKey(), entry.getValue());
                        }
                        registeredConnections.put(DatabaseTypes.REDIS, redisConnectionSet);
                    }

                    break;
            }
        }
        return registeredConnections;
    }

    public  <T extends AbstractConnection> T getConnection(String connectionId, DatabaseTypes databaseType) {
        try {
            Map<String, T> databaseConnection = getRegisteredConnections(databaseType);
            return databaseConnection.get(connectionId);
        } catch (NullPointerException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public Map<DatabaseTypes, Map<String, Object>> findAllDatabaseTypes() {
        Map<DatabaseTypes, Map<String, Object>> map = new LinkedHashMap<DatabaseTypes, Map<String, Object>>();
        for (DatabaseTypes databaseType : databaseConnectionRegistries.keySet()) {
            Map<String, Object> submap = new HashMap<String, Object>();
            submap.put("connectedDatabases", databaseConnectionRegistries.get(databaseType).getRegistry().size());
            submap.put("displayName", databaseType.getDisplayName());
            map.put(databaseType, submap);
        }
        return map;
    }

    public boolean isAvailableId(String id) {
        for (DatabaseConnectionRegistry databaseConnectionRegistry : databaseConnectionRegistries.values()) {
            if (databaseConnectionRegistry.getRegistry().containsKey(id)) {
                return false;
            }
        }
        return true;
    }

    public boolean addEditConnection(final AbstractConnection connection, final Boolean isEdition) {
        return databaseConnectionRegistries.get(connection.getDatabaseType()).addEditConnection(connection, isEdition);
    }

    public boolean removeConnection(String connectionId, DatabaseTypes databaseType) {
        return databaseConnectionRegistries.get(databaseType).removeConnection(connectionId);
    }

    public boolean updateConnection(String connectionId, DatabaseTypes databaseType, boolean connect) {
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
        Map<String, Map> importedConnectionsResults = new LinkedHashMap<>();
        Map<String, String> report = new LinkedHashMap<>();
        try {
            file = File.createTempFile("temporaryImportFile", ".wzd");
            FileUtils.copyInputStreamToFile(source, file);
            dslExecutor.execute(file.toURI().toURL(), dslHandlerMap.get("importConnection"), importedConnectionsResults);
            logger.info("Done importing connections" + importedConnectionsResults);
            report.put("status", "success");
        } catch (FileNotFoundException ex) {
            report.put("status", "error");
            report.put("reason", "fileNotFound");
            logger.error(ex.getMessage(), ex);
        } catch (IOException ex) {
            report.put("status", "error");
            report.put("reason", "io");
            logger.error(ex.getMessage(), ex);
        } catch (Exception ex) {
            report.put("status", "error");
            report.put("reason", "other");
            logger.error(ex.getMessage(), ex);
        } finally {
            FileUtils.deleteQuietly(file);
        }
        importedConnectionsResults.put("report", report);
        return importedConnectionsResults;
    }

    public Map <String, Object> importConnection(Map<String, Object> map) {
        logger.info("Importing connection " + map);
        switch (DatabaseTypes.valueOf((String) map.get("type"))) {
            case MONGO:
                try {
                    if (databaseConnectionRegistries.get(DatabaseTypes.valueOf((String) map.get("type"))).getRegistry().containsKey(map.get("identifier"))) {
                        map.put("status", "failed");
                        map.put("statusMessage", "connectionExists");
                        //Create instance to be able to parse the options of a failed connection.
                        if (map.containsKey("options")) {
                            MongoConnection connection = new MongoConnection((String)map.get("identifier"));
                            map.put("options", map.containsKey("options") ? connection.parseOptions((LinkedHashMap) map.get("options")) : null);
                        }
                    } else {
                        //Create connection object
                        MongoConnection connection = new MongoConnection((String)map.get("identifier"));
                        String host = map.containsKey("host") ? (String) map.get("host") : null;
                        Integer port = map.containsKey("port") ? Integer.parseInt((String) map.get("port")) : null;
                        Boolean isConnected = map.containsKey("isConnected") && Boolean.parseBoolean((String) map.get("isConnected"));
                        String dbName = map.containsKey("dbName") ? (String) map.get("dbName") : null;
                        String user = map.containsKey("user") ? (String) map.get("user") : null;
                        String writeConcern = map.containsKey("writeConcern") ? (String) map.get("writeConcern") : "ACKNOWLEDGE";
                        String authDb = map.containsKey("authDb") ? (String) map.get("authDb") : null;
                        String options = map.containsKey("options") ? connection.parseOptions((LinkedHashMap) map.get("options")) : null;
                        map.put("options", options);
                        String password = (String) map.get("password");
                            if(password != null && password.contains("_ENC")) {
                                password = password.substring(0,32);
                                password = EncryptionUtils.passwordBaseDecrypt(password);
                                map.put("password",password);
                             } else if (password != null && !password.contains("_ENC")) {
                                map.put("password", password);
                            }

                        connection.setHost(host);
                        connection.setPort(port);
                        connection.isConnected(isConnected);
                        connection.setDbName(dbName);
                        connection.setUser(user);
                        connection.setPassword(password);
                        connection.setWriteConcern(writeConcern);
                        connection.setAuthDb(authDb);
                        connection.setOptions(options);
                        databaseConnectionRegistries.get(DatabaseTypes.valueOf((String) map.get("type"))).addEditConnection(connection, false);
                        map.put("status", "success");
                    }

                } catch (Exception ex) {
                    map.put("status", "failed");
                    map.put("statusMessage", "creationFailed");
                    //try to parse options if the exist otherwise we will just remove them.
                    try {
                        if (map.containsKey("options")) {
                            MongoConnection connection = new MongoConnection((String)map.get("identifier"));
                            map.put("options", map.containsKey("options") ? connection.parseOptions((LinkedHashMap) map.get("options")) : null);
                        }
                    } catch (Exception e){
                        map.remove("options");
                    }
                    logger.info("Import " + (map.containsKey("identifier") ? "for connection: '" + map.get("identifier") + "'" : "") + " failed", ex.getMessage(), ex);
                }
                break;
            case REDIS:
                try {
                    if (databaseConnectionRegistries.get(DatabaseTypes.valueOf((String) map.get("type"))).getRegistry().containsKey(map.get("identifier"))) {
                        map.put("statusMessage", "connectionExists");


                    } else {
                        //Create connection object
                        RedisConnection connection = new RedisConnection((String)map.get("identifier"));
                        String host = map.containsKey("host") ? (String) map.get("host") : null;
                        Integer port = map.containsKey("port") ? Integer.parseInt((String) map.get("port")) : null;
                        Boolean isConnected = map.containsKey("isConnected") && Boolean.parseBoolean((String) map.get("isConnected"));
                        String dbName = map.containsKey("dbName") ? (String) map.get("dbName") : null;
                        String options = map.containsKey("options") ? connection.parseOptions((LinkedHashMap) map.get("options")) : null;
                        map.put("options", options);
                        String password = (String) map.get("password");
                        Integer timeout = map.containsKey("timeout") ? Integer.parseInt((String) map.get("timeout")) : null;
                        Integer weight = map.containsKey("weight") ? Integer.parseInt((String) map.get("weight")) : null;

                        if(password != null && password.contains("_ENC")) {
                            password = password.substring(0,32);
                            password = EncryptionUtils.passwordBaseDecrypt(password);
                            map.put("password",password);
                        } else if (password != null && !password.contains("_ENC")) {
                            map.put("password", password);
                        }

                        connection.setHost(host);
                        connection.setPort(port);
                        connection.isConnected(isConnected);
                        connection.setDbName(dbName);
                        connection.setPassword(password);
                        connection.setWeight(weight);
                        connection.setTimeout(timeout);

                        databaseConnectionRegistries.get(DatabaseTypes.valueOf((String) map.get("type"))).addEditConnection(connection, false);
                        map.put("status", "success");
                    }

                } catch (Exception ex) {
                    map.put("status", "failed");
                    map.put("statusMessage", "creationFailed");
                    //try to parse options if the exist otherwise we will just remove them.
                    try {
                        if (map.containsKey("options")) {
                            RedisConnection connection = new RedisConnection((String)map.get("identifier"));
                            map.put("options", map.containsKey("options") ? connection.parseOptions((LinkedHashMap) map.get("options")) : null);
                        }
                    } catch (Exception e){
                        map.remove("options");
                    }
                    logger.info("Import " + (map.containsKey("identifier") ? "for connection: '" + map.get("identifier") + "'" : "") + " failed", ex.getMessage(), ex);
                }
                break;

            default:
                map.put("status", "failed");
                map.put("statusMessage", "invalidDatabaseType");
        }
        return map;
    }

    protected BundleContext getBundleContext() {
        return bundleContext;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setDslExecutor(DSLExecutor dslExecutor) {
        this.dslExecutor = dslExecutor;
    }

    public void setDslHandlerMap(Map<String, DSLHandler> dslHandlerMap) {
        this.dslHandlerMap = dslHandlerMap;
    }

    public File exportConnections (JSONObject connections) throws JSONException {
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
                    sb.append("connection {" + Utils.NEW_LINE);
                    sb.append(getConnection(connectionId, DatabaseTypes.valueOf(type)).getSerializedExportData());
                    sb.append(Utils.NEW_LINE + "}" + Utils.NEW_LINE);
                }
            }
            FileUtils.writeStringToFile(file, sb.toString(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public Map<String, Object> getServerStatus(String connectionId, DatabaseTypes databaseType ) {
        AbstractConnection connection = getConnection(connectionId, databaseType);
        Map<String, Object> serverStatus = new LinkedHashMap<>();
        if (!connection.isConnected()) {
            serverStatus.put("failed", "Connection is disconnected");
            return serverStatus;
        }
        serverStatus.put("success", connection.getServerStatus());
        return serverStatus;

    }
}
