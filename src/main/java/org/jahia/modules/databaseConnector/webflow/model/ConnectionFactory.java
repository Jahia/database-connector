package org.jahia.modules.databaseConnector.webflow.model;

import org.jahia.modules.databaseConnector.ConnectionData;
import org.jahia.modules.databaseConnector.DatabaseTypes;
import org.jahia.modules.databaseConnector.webflow.model.mongo.MongoConnectionImpl;
import org.jahia.modules.databaseConnector.webflow.model.neo4j.Neo4jConnection;
import org.jahia.modules.databaseConnector.webflow.model.redis.RedisConnectionImpl;

import static org.jahia.modules.databaseConnector.DatabaseTypes.valueOf;

/**
 * Date: 11/19/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class ConnectionFactory {

    public static Connection makeConnection(String databaseTypeName) {
        return makeConnection(valueOf(databaseTypeName));
    }

    public static Connection makeConnection(DatabaseTypes databaseType) {
        Connection connection = null;
        switch (databaseType) {
            case NEO4J:
                connection = new Neo4jConnection();
                break;
            case REDIS:
                connection = new RedisConnectionImpl();
                break;
            case MONGO:
                connection = new MongoConnectionImpl();
                break;
        }
        return connection;
    }

    public static Connection makeConnection(ConnectionData connectionData) {
        Connection connection = null;
        switch (connectionData.getDatabaseType()) {
            case NEO4J:
                connection = new Neo4jConnection(connectionData);
                break;
            case REDIS:
                connection = new RedisConnectionImpl(connectionData);
                break;
            case MONGO:
                connection = new MongoConnectionImpl(connectionData);
                break;
        }
        return connection;
    }
}
