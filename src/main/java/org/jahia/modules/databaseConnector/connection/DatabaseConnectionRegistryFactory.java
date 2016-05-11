package org.jahia.modules.databaseConnector.connection;

import org.jahia.modules.databaseConnector.connection.mongo.MongoConnectionRegistry;
import org.jahia.modules.databaseConnector.connection.redis.RedisConnectionRegistry;

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
            case MONGO:
                databaseConnectionRegistry = new MongoConnectionRegistry();
                break;
            case REDIS:
                databaseConnectionRegistry = new RedisConnectionRegistry();
                break;
        }
        databaseConnectionRegistry.populateRegistry();
        return databaseConnectionRegistry;
    }
}
