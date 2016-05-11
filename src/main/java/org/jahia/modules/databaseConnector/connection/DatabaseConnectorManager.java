package org.jahia.modules.databaseConnector.connection;

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.jahia.modules.databaseConnector.connection.mongo.MongoConnection;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

import static org.jahia.modules.databaseConnector.connection.DatabaseTypes.getAllDatabaseTypes;
import static org.jahia.modules.databaseConnector.connection.DatabaseTypes.valueOf;

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
                for (Object databaseId : set) {
                    ((AbstractConnection) registry.get(databaseId)).registerAsService();
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

    public  <T extends AbstractConnection> T getConnection(String databaseId, DatabaseTypes databaseType) {
        try {
            Map<String, T> databaseConnection = getRegisteredConnections(databaseType);
            return databaseConnection.get(databaseId);
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

    public boolean removeConnection(String databaseId, String databaseTypeName) {
        DatabaseTypes databaseType = valueOf(databaseTypeName);
        return databaseConnectionRegistries.get(databaseType).removeConnection(databaseId);
    }


    protected BundleContext getBundleContext() {
        return bundleContext;
    }
}
