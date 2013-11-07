package org.jahia.modules.databaseConnector;

import org.jahia.modules.databaseConnector.redis.RedisDatabaseConnection;
import org.jahia.modules.databaseConnector.webflow.model.Connection;

import java.util.Map;

/**
 * Date: 11/6/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public interface DatabaseConnectionRegistry<T extends AbstractDatabaseConnection> {

    public Map<String, T> getRegistry();

    public Map<String, T> populateRegistry();

    public void addConnection(Connection connection);

    public Boolean removeConnection(String databaseConnectionId);

//    public void editConnection(Connection connection);

//    public DatabaseTypes getDatabaseType();
}
