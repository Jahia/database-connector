package org.jahia.modules.databaseConnector.connection;

import org.apache.commons.io.FileUtils;
import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.jahia.modules.databaseConnector.connection.mongo.MongoConnection;
import org.jahia.modules.databaseConnector.dsl.DSLExecutor;
import org.jahia.modules.databaseConnector.dsl.DSLHandler;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.*;
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

    private static DatabaseConnectorManager instance;

    private BundleContext bundleContext;

    private DSLExecutor dslExecutor;

    private Map<String,DSLHandler> dslHandlerMap;

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectorManager.class);

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

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
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
                    //@TODO Register REDIS connection
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

    public boolean executeConnectionImportHandler(InputStream source) {
        File file = null;
        FileInputStream fileInputStream = null;
        try {
            file = File.createTempFile("temporaryImportFile", ".wzd");
            FileUtils.copyInputStreamToFile(source, file);
            dslExecutor.execute(file.toURI().toURL(), dslHandlerMap.get("importConnection"));
        } catch (FileNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return true;
    }

    public boolean importConnections(Map<String, Object> map) {
        logger.info("Importing connection " + map);
        return true;
    }

    protected BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setDslExecutor(DSLExecutor dslExecutor) {
        this.dslExecutor = dslExecutor;
    }

    public void setDslHandlerMap(Map<String, DSLHandler> dslHandlerMap) {
        this.dslHandlerMap = dslHandlerMap;
    }
}
