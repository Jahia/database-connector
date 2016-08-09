package org.jahia.modules.databaseConnector.connection;

import java.util.Map;

/**
 * Date: 11/6/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public interface DatabaseConnectionRegistry<T> {

    Map<String, T> getRegistry();

    Map<String, T> populateRegistry();

    boolean addEditConnection(final AbstractConnection connection, final Boolean isEdition);

    boolean importConnection(final Map<String, Object> map);

    boolean testConnection(final AbstractConnection connection);

    boolean removeConnection(String databaseConnectionId);

    boolean connect(final String databaseConnectionId);

    boolean disconnect(final String databaseConnectionId);
}
