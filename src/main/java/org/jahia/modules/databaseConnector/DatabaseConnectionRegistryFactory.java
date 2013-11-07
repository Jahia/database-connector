package org.jahia.modules.databaseConnector;

import org.jahia.modules.databaseConnector.mongo.MongoDatabaseConnectionRegistry;
import org.jahia.modules.databaseConnector.neo4j.Neo4jDatabaseConnectionRegistry;
import org.jahia.modules.databaseConnector.redis.RedisDatabaseConnectionRegistry;

/**
 * Date: 11/6/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class DatabaseConnectionRegistryFactory {

    public static DatabaseConnectionRegistry makeDatabaseConnectionRegistry(DatabaseTypes databaseType) {
        DatabaseConnectionRegistry databaseConnectionRegistry = null;
        switch (databaseType) {
            case NEO4J:
                databaseConnectionRegistry = new Neo4jDatabaseConnectionRegistry();
                break;
            case MONGO:
                databaseConnectionRegistry = new MongoDatabaseConnectionRegistry();
                break;
            case REDIS:
                databaseConnectionRegistry = new RedisDatabaseConnectionRegistry();
                break;
        }
        databaseConnectionRegistry.populateRegistry();
        return databaseConnectionRegistry;
    }
}
