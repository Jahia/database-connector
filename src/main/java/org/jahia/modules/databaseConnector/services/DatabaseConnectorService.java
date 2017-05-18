package org.jahia.modules.databaseConnector.services;

import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.connection.ConnectionData;

import java.util.Map;

/**
 * Created by stefan on 2017-04-19.
 */
public interface DatabaseConnectorService {

    <T extends AbstractConnection> T getConnection(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException;

    <T extends AbstractConnection> String getConnectionAsString(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException;

    <T extends AbstractConnection> Map<String, T> getConnections(String databaseType) throws InstantiationException, IllegalAccessException;

    <T extends AbstractConnection, E extends ConnectionData> String getConnectionsAsString(String databaseType) throws InstantiationException, IllegalAccessException;

    <T extends AbstractConnection> boolean hasConnection(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException;
    boolean addEditConnection(AbstractConnection connection, Boolean isEdition);

    boolean testConnection(AbstractConnection connection);

    boolean removeConnection(String connectionId, String databaseType);

    boolean updateConnection(String connectionId, String databaseType, boolean connect);

    boolean isConnectionIdAvailable(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException;

    Map<String, Object> getServerStatus(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException;

    String setPassword(Map<String, Object> map, String password);

}
