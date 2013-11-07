package org.jahia.modules.databaseConnector;

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.jahia.modules.databaseConnector.neo4j.Neo4jDatabaseConnection;
import org.jahia.modules.databaseConnector.neo4j.Neo4jDatabaseConnectionRegistry;
import org.jahia.modules.databaseConnector.redis.RedisDatabaseConnection;
import org.jahia.modules.databaseConnector.redis.RedisDatabaseConnectionRegistry;
import org.jahia.modules.databaseConnector.webflow.model.Connection;
import org.jahia.services.content.JCRTemplate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.rest.SpringRestGraphDatabase;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.Assert;

import java.util.*;

import static org.jahia.modules.databaseConnector.DatabaseTypes.*;

/**
 * Date: 2013-10-17
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class DatabaseConnectorManager implements DatabaseConnectorOSGiService, BundleContextAware {

    private BundleContext bundleContext;

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectorManager.class);

    public static final String DATABASE_CONNECTOR_ROOT_PATH = "/settings/";

    public static final String DATABASE_CONNECTOR_PATH = "databaseConnector";

    public static final String DATABASE_CONNECTOR_NODE_TYPE = "dc:databaseConnector";

    private JCRTemplate jcrTemplate;

    private Map<DatabaseTypes, DatabaseConnectionRegistry> databaseConnectionRegistries;

    private Set<DatabaseTypes> activatedDatabaseTypes = getAllDatabaseTypes();

    public DatabaseConnectorManager() {
        this.jcrTemplate = JCRTemplate.getInstance();

        databaseConnectionRegistries = new TreeMap<DatabaseTypes, DatabaseConnectionRegistry>();
        for (DatabaseTypes activatedDatabaseType : activatedDatabaseTypes) {
            DatabaseConnectionRegistry databaseConnectionRegistry
                    = DatabaseConnectionRegistryFactory.makeDatabaseConnectionRegistry(activatedDatabaseType);
            databaseConnectionRegistries.put(activatedDatabaseType, databaseConnectionRegistry);
        }
    }

    @Override
    public boolean registerNeo4jGraphDatabase(String databaseId) throws InvalidSyntaxException {
        logger.info("Start registering OSGi service for Neo4j Template");
        Assert.isTrue(databaseConnectionRegistries.containsKey(NEO4J), "No Neo4j connection registered");
        final Map<String, Neo4jDatabaseConnection> registry = ((Neo4jDatabaseConnectionRegistry) databaseConnectionRegistries.get(NEO4J)).getRegistry();
        Assert.isTrue(registry.containsKey(databaseId), "No Neo4j connection registered with databaseId: " + databaseId);
        SpringRestGraphDatabase graphDatabaseService = registry.get(databaseId).getGraphDatabaseService();
        ServiceReference[] serviceReferences =
                bundleContext.getAllServiceReferences(graphDatabaseService.getClass().getName(), createFilter(NEO4J, databaseId));
        if(serviceReferences != null) {
            logger.info("OSGi service for Neo4j Template already registered");
            return true;
        }
        bundleContext.registerService(graphDatabaseService.getClass().getName(), graphDatabaseService, createProperties(NEO4J, databaseId));
        logger.info("OSGi service for Neo4j Template successfully registered");
        return true;
    }

    @Override
    public boolean registerRedisConnectionFactory(String databaseId) throws InvalidSyntaxException {
        logger.info("Start registering OSGi service for Redis Connection Factory");
        Assert.isTrue(databaseConnectionRegistries.containsKey(REDIS), "No Redis connection registered");
        final Map<String, RedisDatabaseConnection> registry = ((RedisDatabaseConnectionRegistry) databaseConnectionRegistries.get(REDIS)).getRegistry();
        Assert.isTrue(registry.containsKey(databaseId), "No Redis connection registered with databaseId: " + databaseId);
        RedisConnectionFactory redisConnectionFactory = registry.get(databaseId).getConnectionFactory();
        ServiceReference[] serviceReferences = bundleContext.getAllServiceReferences(RedisConnectionFactory.class.getName(), createFilter(REDIS, databaseId));
        if(serviceReferences != null) {
            logger.info("OSGi service for Redis Connection Factory already registered");
            return true;
        }
        bundleContext.registerService(getInterfacesNames(redisConnectionFactory), redisConnectionFactory, createProperties(REDIS, databaseId));
        logger.info("OSGi service for Redis Connection Factory successfully registered");
        return true;
    }

    @Override
    public boolean registerRedisStringTemplate(String databaseId) throws InvalidSyntaxException {
//        logger.info("Start registering OSGi service for Redis String Templat");
//        if (!redisDatabaseConnectionRegistry.containsKey(databaseId)) {
//            throw new IllegalArgumentException("No Redis Connection registered with databaseId: " + databaseId);
//        }
//        StringRedisTemplate stringRedisTemplate = redisDatabaseConnectionRegistry.get(databaseId).getStringRedisTemplate();
//        ServiceReference[] serviceReferences = bundleContext.getAllServiceReferences(stringRedisTemplate.getClass().getName(), createFilter(REDIS, databaseId));
//        if(serviceReferences != null) {
//            logger.info("OSGi service for Redis String Template already registered");
//            return true;
//        }
//        bundleContext.registerService(stringRedisTemplate.getClass().getName(), stringRedisTemplate, createProperties(REDIS, databaseId));
//        logger.info("OSGi service for Redis String Template successfully registered");
        return true;
    }

    @Override
    public boolean registerRedisLongTemplate(String databaseId) throws InvalidSyntaxException {
//        logger.info("Start registering OSGi service for Redis Long Template");
//        if (!redisDatabaseConnectionRegistry.containsKey(databaseId)) {
//            throw new IllegalArgumentException("No Redis Connection registered with databaseId: " + databaseId);
//        }
//        RedisTemplate<String, Long> longRedisTemplate = redisDatabaseConnectionRegistry.get(databaseId).getLongRedisTemplate();
//        ServiceReference[] serviceReferences = bundleContext.getAllServiceReferences(longRedisTemplate.getClass().getName(), createFilter(REDIS, databaseId));
//        if(serviceReferences != null) {
//            logger.info("OSGi service for Redis Long Template already registered");
//            return true;
//        }
//        bundleContext.registerService(longRedisTemplate.getClass().getName(), longRedisTemplate, createProperties(REDIS, databaseId));
//        logger.info("OSGi service for Redis Long Template successfully registered");
        return true;
    }

    @Override
    public boolean registerRedisIntegerTemplate(String databaseId) throws InvalidSyntaxException {
//        logger.info("Start registering OSGi service for Redis Integer Template");
//        if (!redisDatabaseConnectionRegistry.containsKey(databaseId)) {
//            throw new IllegalArgumentException("No Redis Connection registered with databaseId: " + databaseId);
//        }
//        RedisTemplate<String, Integer> integerRedisTemplate = redisDatabaseConnectionRegistry.get(databaseId).getIntegerRedisTemplate();
//        ServiceReference[] serviceReferences = bundleContext.getAllServiceReferences(integerRedisTemplate.getClass().getName(), createFilter(REDIS, databaseId));
//        if(serviceReferences != null) {
//            logger.info("OSGi service for Redis Integer Template already registered");
//            return true;
//        }
//        bundleContext.registerService(integerRedisTemplate.getClass().getName(), integerRedisTemplate, createProperties(REDIS, databaseId));
//        logger.info("OSGi service for Redis Integer Template successfully registered");
        return true;
    }

    @Override
    public boolean registerMongoTemplate(String databaseId) throws InvalidSyntaxException {
//        logger.info("Start registering OSGi service for MongoDB Template");
//        if (!mongoDatabaseConnectionRegistry.containsKey(databaseId)) {
//            throw new IllegalArgumentException("No Mongo Connection registered with databaseId: " + databaseId);
//        }
//        MongoTemplate mongoTemplate = mongoDatabaseConnectionRegistry.get(databaseId).getTemplate();
//        ServiceReference[] serviceReferences = bundleContext.getAllServiceReferences(mongoTemplate.getClass().getName(), createFilter(MONGO, databaseId));
//        if(serviceReferences != null) {
//            logger.info("OSGi service for MongoDB Template already registered");
//            return true;
//        }
//        bundleContext.registerService(mongoTemplate.getClass().getName(), mongoTemplate, createProperties(MONGO, databaseId));
//        logger.info("OSGi service for MongoDB Template successfully registered");
        return true;
    }

    @Override
    public boolean registerMongoDbFactory(String databaseId) throws InvalidSyntaxException {
//        logger.info("Start registering OSGi service for MongoDB Factory");
//        if (!mongoDatabaseConnectionRegistry.containsKey(databaseId)) {
//            throw new IllegalArgumentException("No Mongo Connection registered with databaseId: " + databaseId);
//        }
//        MongoDbFactory mongoDbFactory = mongoDatabaseConnectionRegistry.get(databaseId).getDbFactory();
//        ServiceReference[] serviceReferences = bundleContext.getAllServiceReferences(MongoDbFactory.class.getName(), createFilter(MONGO, databaseId));
//        if(serviceReferences != null) {
//            logger.info("OSGi service for MongoDB Factory already registered");
//            return true;
//        }
//        bundleContext.registerService(getInterfacesNames(mongoDbFactory), mongoDbFactory, createProperties(MONGO, databaseId));
//        logger.info("OSGi service for MongoDB Factory successfully registered");
        return true;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    private String[] getInterfacesNames(Object obj) {
        Class[] interfaces = obj.getClass().getInterfaces();
        String[] interfacesNames = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            interfacesNames[i] = interfaces[i].getName();
        }
        return interfacesNames;
    }

    public Map<DatabaseTypes, Set<ConnectionData>> findRegisteredConnections() {
        Map<DatabaseTypes, Set<ConnectionData>> registeredConnections = new LinkedHashMap<DatabaseTypes, Set<ConnectionData>>();
        for (DatabaseTypes databaseType : databaseConnectionRegistries.keySet()) {
            Map<String, AbstractDatabaseConnection> registry =
                    (Map<String, AbstractDatabaseConnection>) databaseConnectionRegistries.get(databaseType).getRegistry();
            Set<ConnectionData> connectionDataSet = new LinkedHashSet<ConnectionData>();
            for (AbstractDatabaseConnection abstractDatabaseConnection : registry.values()) {
                connectionDataSet.add(abstractDatabaseConnection.createData());
            }
            registeredConnections.put(databaseType, connectionDataSet);
        }
        return registeredConnections;
    }

    public static String createFilter(DatabaseTypes databaseType, String id) {
        StringBuffer sb = new StringBuffer("(&(").append(getKey()).append("=").append(databaseType.name())
                .append(")(").append(DatabaseTypes.getKey()).append("=").append(id).append("))");
        return sb.toString();
    }

    private Properties createProperties(DatabaseTypes databaseType, String id) {
        Properties properties = new Properties();
        properties.put(getKey(), databaseType.name());
        properties.put(DatabaseTypes.getKey(), id);
        return properties;
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
            AbstractDatabaseConnection databaseConnection =
                    ((Map<String, AbstractDatabaseConnection>) databaseConnectionRegistries.get(databaseType).getRegistry()).get(databaseId);
            return databaseConnection.createData();
        } catch (NullPointerException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

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

    public void addEditConnection(Connection connection, boolean isEdition) {
        databaseConnectionRegistries.get(connection.getDatabaseType()).addEditConnection(connection, isEdition);
    }

    public void removeConnection(String databaseId, String databaseTypeName) {
        DatabaseTypes databaseType = valueOf(databaseTypeName);
        databaseConnectionRegistries.get(databaseType).removeConnection(databaseId);
    }

    public void setActivatedDatabaseTypes(Set<DatabaseTypes> activatedDatabaseTypes) {
        this.activatedDatabaseTypes = activatedDatabaseTypes;
    }
}
