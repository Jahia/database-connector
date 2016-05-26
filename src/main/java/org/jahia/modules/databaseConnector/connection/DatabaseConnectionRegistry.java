package org.jahia.modules.databaseConnector.connection;

import java.util.Map;

/**
 * Date: 11/6/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public interface DatabaseConnectionRegistry<T>{

    public Map<String, T> getRegistry();

    public Map<String, T> populateRegistry();

    public boolean addEditConnection(final AbstractConnection connection, final Boolean isEdition);

    public boolean importConnection(final Map<String, Object> map);

    public boolean testConnection(final AbstractConnection connection);

    public boolean removeConnection(String databaseConnectionId);

    public boolean connect(final String databaseConnectionId);

    public boolean disconnect(final String databaseConnectionId);
}
