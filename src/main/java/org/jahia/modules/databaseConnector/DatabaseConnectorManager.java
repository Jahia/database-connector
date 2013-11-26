package org.jahia.modules.databaseConnector;

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.jahia.modules.databaseConnector.webflow.model.Connection;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.*;

import static org.jahia.modules.databaseConnector.DatabaseTypes.getAllDatabaseTypes;
import static org.jahia.modules.databaseConnector.DatabaseTypes.valueOf;

/**
 * Date: 2013-10-17
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class DatabaseConnectorManager implements DatabaseConnectorOSGiService, BundleContextAware, InitializingBean {

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
    public void afterPropertiesSet() throws Exception {
        for (DatabaseTypes activatedDatabaseType : activatedDatabaseTypes) {
            DatabaseConnectionRegistry databaseConnectionRegistry
                    = DatabaseConnectionRegistryFactory.makeDatabaseConnectionRegistry(activatedDatabaseType);
            databaseConnectionRegistries.put(activatedDatabaseType, databaseConnectionRegistry);
        }
    }

    @Override
    public boolean registerSingleDatabase(DatabaseTypes databaseType) {
        return getOneDatabaseConnection(databaseType).registerAsService();
    }

    @Override
    public boolean registerDatabase(String databaseId, DatabaseTypes databaseType) {
        return getDatabaseConnection(databaseId, databaseType).registerAsService();
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractDatabaseConnection> Map<String, T> getDatabaseRegistry(DatabaseTypes databaseType) {
        Assert.isTrue(databaseConnectionRegistries.containsKey(databaseType),
                "No " + databaseType.getDisplayName() + " connection registered");
        return databaseConnectionRegistries.get(databaseType).getRegistry();
    }

    private <T extends AbstractDatabaseConnection> T getOneDatabaseConnection(DatabaseTypes databaseType) {
        Map<String, T> databaseRegistry = getDatabaseRegistry(databaseType);
        Assert.isTrue(databaseRegistry.size() == 1,
                "None or more than one DatabaseConnection of type " + databaseType.getDisplayName() + " registered");
        return databaseRegistry.values().iterator().next();
    }

    private <T extends AbstractDatabaseConnection> T getDatabaseConnection(String databaseId, DatabaseTypes databaseType) {
        Map<String, T> databaseRegistry = getDatabaseRegistry(databaseType);
        Assert.isTrue(databaseRegistry.containsKey(databaseId),
                "No " + databaseType.getDisplayName() + " connection with id '" + databaseId + "'registered");
        return databaseRegistry.get(databaseId);
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public Map<DatabaseTypes, Set<ConnectionData>> findRegisteredConnections() {
        Map<DatabaseTypes, Set<ConnectionData>> registeredConnections = new LinkedHashMap<DatabaseTypes, Set<ConnectionData>>();
        for (DatabaseTypes databaseType : databaseConnectionRegistries.keySet()) {
            Map<String, AbstractDatabaseConnection> registry = getDatabaseRegistry(databaseType);
            Set<ConnectionData> connectionDataSet = new LinkedHashSet<ConnectionData>();
            for (AbstractDatabaseConnection abstractDatabaseConnection : registry.values()) {
                connectionDataSet.add(abstractDatabaseConnection.makeConnectionData());
            }
            registeredConnections.put(databaseType, connectionDataSet);
        }
        return registeredConnections;
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

    public ConnectionData getConnectionData(String databaseId, String databaseTypeName) {
        try {
            DatabaseTypes databaseType = valueOf(databaseTypeName);
            AbstractDatabaseConnection databaseConnection = getDatabaseRegistry(databaseType).get(databaseId);
            return databaseConnection.makeConnectionData();
        } catch (NullPointerException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> getUsedIds() {
        HashSet<String> usedIds = new HashSet<String>();
        for (DatabaseConnectionRegistry databaseConnectionRegistry : databaseConnectionRegistries.values()) {
            for (AbstractDatabaseConnection databaseConnection : ((Map<String, AbstractDatabaseConnection>) databaseConnectionRegistry.getRegistry()).values()) {
                usedIds.add(databaseConnection.getId());
            }
        }
        return usedIds;
    }

    public String getNextAvailableId(DatabaseTypes databaseType) {
        String initialId = databaseType.name().toLowerCase();
        Set<String> usedIds = getUsedIds();
        if (!usedIds.contains(initialId)) {
            return initialId;
        }
        int index = 1;
        while (usedIds.contains(initialId+index)) {
            index++;
        }
        return initialId+index;
    }

    public boolean isAvailableId(String id, DatabaseTypes databaseType) {
        return !databaseConnectionRegistries.get(databaseType).getRegistry().containsKey(id);
    }

    public void addEditConnection(final Connection connection, final Boolean isEdition) {
        databaseConnectionRegistries.get(connection.getDatabaseType()).addEditConnection(connection, isEdition);
    }

    public void removeConnection(String databaseId, String databaseTypeName) {
        DatabaseTypes databaseType = valueOf(databaseTypeName);
        databaseConnectionRegistries.get(databaseType).removeConnection(databaseId);
    }

    public void setActivatedDatabaseTypes(Set<DatabaseTypes> activatedDatabaseTypes) {
        this.activatedDatabaseTypes = activatedDatabaseTypes;
    }

    protected BundleContext getBundleContext() {
        return bundleContext;
    }
}
