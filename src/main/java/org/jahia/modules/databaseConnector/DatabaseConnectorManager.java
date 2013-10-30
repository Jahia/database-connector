package org.jahia.modules.databaseConnector;

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.neo4j.rest.SpringRestGraphDatabase;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Properties;

/**
 * Date: 2013-10-17
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class DatabaseConnectorManager implements DatabaseConnectorOSGiService, BundleContextAware {

    private BundleContext bundleContext;

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectorManager.class);

    @Autowired
    private SpringRestGraphDatabase graphDatabaseService;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Long> longRedisTemplate;

    @Autowired
    private RedisTemplate<String, Integer> integerRedisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoDbFactory mongoDbFactory;

    public DatabaseConnectorManager() {}

    @Override
    public boolean registerNeo4jGraphDatabase() throws InvalidSyntaxException {
        logger.info("Start registering OSGi service for Neo4j Template");
        ServiceReference[] serviceReferences = bundleContext.getAllServiceReferences(graphDatabaseService.getClass().getName(), "(database=neo4j)");
        if(serviceReferences != null) {
            logger.info("OSGi service for Neo4j Template already registered");
            return true;
        }
        Properties properties = new Properties();
        properties.put("database", "neo4j");
        bundleContext.registerService(graphDatabaseService.getClass().getName(), graphDatabaseService, properties);
        logger.info("OSGi service for Neo4j Template successfully registered");
        return true;
    }

    @Override
    public boolean registerRedisConnectionFactory() throws InvalidSyntaxException {
        logger.info("Start registering OSGi service for Redis Connection Factory");
        ServiceReference[] serviceReferences = bundleContext.getAllServiceReferences(RedisConnectionFactory.class.getName(), "(database=redis)");
        if(serviceReferences != null) {
            logger.info("OSGi service for Redis Connection Factory already registered");
            return true;
        }
        Properties properties = new Properties();
        properties.put("database", "redis");
        bundleContext.registerService(getInterfacesNames(redisConnectionFactory), redisConnectionFactory, properties);
        logger.info("OSGi service for Redis Connection Factory successfully registered");
        return true;
    }

    @Override
    public boolean registerRedisStringTemplate() throws InvalidSyntaxException {
        logger.info("Start registering OSGi service for Redis String Template");
        ServiceReference[] serviceReferences = bundleContext.getAllServiceReferences(stringRedisTemplate.getClass().getName(), "(database=redis)");
        if(serviceReferences != null) {
            logger.info("OSGi service for Redis String Template already registered");
            return true;
        }
        Properties properties = new Properties();
        properties.put("database", "redis");
        bundleContext.registerService(stringRedisTemplate.getClass().getName(), stringRedisTemplate, properties);
        logger.info("OSGi service for Redis String Template successfully registered");
        return true;
    }

    @Override
    public boolean registerRedisLongTemplate() throws InvalidSyntaxException {
        logger.info("Start registering OSGi service for Redis Long Template");
        ServiceReference[] serviceReferences = bundleContext.getAllServiceReferences(longRedisTemplate.getClass().getName(), "(database=redis)");
        if(serviceReferences != null) {
            logger.info("OSGi service for Redis Long Template already registered");
            return true;
        }
        Properties properties = new Properties();
        properties.put("database", "redis");
        bundleContext.registerService(longRedisTemplate.getClass().getName(), longRedisTemplate, properties);
        logger.info("OSGi service for Redis Long Template successfully registered");
        return true;
    }

    @Override
    public boolean registerRedisIntegerTemplate() throws InvalidSyntaxException {
        logger.info("Start registering OSGi service for Redis Integer Template");
        ServiceReference[] serviceReferences = bundleContext.getAllServiceReferences(integerRedisTemplate.getClass().getName(), "(database=redis)");
        if(serviceReferences != null) {
            logger.info("OSGi service for Redis Integer Template already registered");
            return true;
        }
        Properties properties = new Properties();
        properties.put("database", "redis");
        bundleContext.registerService(integerRedisTemplate.getClass().getName(), integerRedisTemplate, properties);
        logger.info("OSGi service for Redis Integer Template successfully registered");
        return true;
    }

    @Override
    public boolean registerMongoTemplate() throws InvalidSyntaxException {
        logger.info("Start registering OSGi service for MongoDB Template");
        ServiceReference[] serviceReferences = bundleContext.getAllServiceReferences(mongoTemplate.getClass().getName(), "(database=mongo)");
        if(serviceReferences != null) {
            logger.info("OSGi service for MongoDB Template already registered");
            return true;
        }
        Properties properties = new Properties();
        properties.put("database", "mongo");
        bundleContext.registerService(mongoTemplate.getClass().getName(), mongoTemplate, properties);
        logger.info("OSGi service for MongoDB Template successfully registered");
        return true;
    }

    @Override
    public boolean registerMongoDbFactory() throws InvalidSyntaxException {
        logger.info("Start registering OSGi service for MongoDB Factory");
        ServiceReference[] serviceReferences = bundleContext.getAllServiceReferences(MongoDbFactory.class.getName(), "(database=mongo)");
        if(serviceReferences != null) {
            logger.info("OSGi service for MongoDB Factory already registered");
            return true;
        }
        Properties properties = new Properties();
        properties.put("database", "mongo");
        bundleContext.registerService(getInterfacesNames(mongoDbFactory), mongoDbFactory, properties);
        logger.info("OSGi service for MongoDB Factory successfully registered");
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

}
