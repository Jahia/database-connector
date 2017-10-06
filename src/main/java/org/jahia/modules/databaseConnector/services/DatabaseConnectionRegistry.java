package org.jahia.modules.databaseConnector.services;

import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.connector.ConnectorMetaData;
import org.jahia.services.content.JCRNodeWrapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.ServiceRegistration;

import javax.jcr.RepositoryException;
import java.util.Map;

/**
 * Date: 11/6/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public interface DatabaseConnectionRegistry<T> {

    Map<String, T> getRegistry();

    Map<String, ServiceRegistration> getRegistrations();

    Map<String, T> populateRegistry();

    boolean addEditConnection(final AbstractConnection connection, final Boolean isEdition);

    boolean addEditConnectionNoStore (final AbstractConnection connection, final Boolean isEdition);

    void importConnection(final Map<String, Object> map);

    boolean testConnection(final AbstractConnection connection);

    boolean removeConnection(String databaseConnectionId);

    void unregisterAndRemoveFromRegistry(String databaseConnectionId);

    boolean connect(final String databaseConnectionId);

    boolean disconnect(final String databaseConnectionId);

    ConnectorMetaData getConnectorMetaData();

    String getConnectionType();

    String getConnectionDisplayName();

    String getEntryPoint();

    Map<String, Object> prepareConnectionMapFromJSON(final Map<String, Object> result, final JSONObject jsonConnectionData) throws JSONException;

    Map<String, Object> prepareConnectionMapFromConnection (final AbstractConnection connection);

    void buildConnectionMapFromJSON(Map<String, Object> result, JSONObject jsonConnectionData) throws JSONException;

    Map<String, Object> buildConnectionMapFromConnection(AbstractConnection connection) throws JSONException;

    AbstractConnection getConnection(String connectionId);

    AbstractConnection nodeToConnection(JCRNodeWrapper connectionNode) throws RepositoryException;

    Object getConnectionService(String databaseType, String connectionId);

    Object getConnectionService(Class c, String databaseType, String connectionId);

    void registerServices();
}
