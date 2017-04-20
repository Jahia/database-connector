package org.jahia.modules.databaseConnector.services;

import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.connection.ConnectionData;

import org.jahia.modules.databaseConnector.connection.DatabaseTypes;
import org.json.JSONException;

import java.util.Map;

/**
 * Created by stefan on 2017-04-19.
 */
public interface DatabaseConnectorService {

    void registerConnectorToRegistry(String connectionType, Class connectionClass);
    
    <T extends AbstractConnection> T getConnection(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException;

    <T extends AbstractConnection> String getConnectionAsString(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException;

    <T extends AbstractConnection> Map<String, T> getConnections(String databaseType) throws InstantiationException, IllegalAccessException;

    <T extends AbstractConnection, E extends ConnectionData> String getConnectionsAsString(String databaseType) throws InstantiationException, IllegalAccessException;

    boolean addEditConnection(AbstractConnection connection, Boolean isEdition);

    boolean testConnection(AbstractConnection connection);

    boolean removeConnection(String connectionId, DatabaseTypes databaseType);

    boolean updateConnection(String connectionId, DatabaseTypes databaseType, boolean connect);

    boolean isConnectionIdAvailable(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException;

    Map<String, Object> getServerStatus(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException;
}
