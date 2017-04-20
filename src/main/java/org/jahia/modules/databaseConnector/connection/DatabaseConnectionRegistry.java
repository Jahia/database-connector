package org.jahia.modules.databaseConnector.connection;

import org.json.JSONException;
import org.json.JSONObject;

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

    String getConnectionType();

    String getConnectionDisplayName();

    Map<String, Object> prepareConnectionMapFromJSON(Map<String, Object> result, JSONObject jsonConnectionData) throws JSONException;

    Map<String, Object> prepareConnectionMapFromConnection (AbstractConnection connection);
}
