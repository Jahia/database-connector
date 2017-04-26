package org.jahia.modules.databaseConnector.api.impl;

import org.jahia.modules.databaseConnector.connection.*;
import org.jahia.modules.databaseConnector.connector.AbstractConnectorMetaData;
import org.jahia.modules.databaseConnector.factories.DatabaseConnectionRegistryFactory;
import org.jahia.modules.databaseConnector.serialization.models.DbConnections;
import org.jahia.modules.databaseConnector.connection.DatabaseConnectorManager;
import org.jahia.modules.databaseConnector.services.DatabaseConnectorService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author stefan on 2016-05-04.
 */
@Component(service = DatabaseConnectorService.class)
public class DatabaseConnector implements DatabaseConnectorService {
    private final static Logger logger = LoggerFactory.getLogger(DatabaseConnector.class);

    private transient DatabaseConnectorManager databaseConnectorManager;
    private BundleContext context;
    @Activate
    public void activate(BundleContext context) {
        this.context = context;
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, service = DatabaseConnectorManager.class)
    public void setDatabaseConnectorManager(DatabaseConnectorManager databaseConnectorManager) {
        this.databaseConnectorManager = databaseConnectorManager;
    }
    public <T extends ConnectionData> String getAllConnections() throws JSONException, InstantiationException, IllegalAccessException{
        String connections;

        Map<String, AbstractConnection> allConnections = new HashMap<>();

        for (Map.Entry<String, DatabaseConnectionRegistry> entry : DatabaseConnectionRegistryFactory.getRegisteredConnections().entrySet()) {
            Map<String, ? extends AbstractConnection> databaseTypeConnection = databaseConnectorManager.getConnections(entry.getKey());
            if (databaseTypeConnection != null) {
                allConnections.putAll(databaseTypeConnection);
            }
        }
        List<ConnectionData> connectionArray = new ArrayList<>();
        for (Map.Entry<String, AbstractConnection> entry : allConnections.entrySet()) {
            connectionArray.add(entry.getValue().makeConnectionData());
        }
        connections = new DbConnections(connectionArray).getJson();
        return connections == null ? new JSONArray().toString() : connections;
    }

    public String getConnectorsMetaData() throws JSONException {
        JSONObject connectorsMetaData = new JSONObject();
        for ( Map.Entry<String, AbstractConnectorMetaData> entry: this.databaseConnectorManager.getAvailableConnectors().entrySet()) {
            connectorsMetaData.put(entry.getKey(), new JSONObject(entry.getValue().getJson()));
        }
        return connectorsMetaData.toString();
    }

    public File exportConnections(JSONObject data)
            throws IOException, RepositoryException, JSONException, InstantiationException, IllegalAccessException{
        return databaseConnectorManager.exportConnections(data);
    }

    public Map importConnections(InputStream source) {
        return databaseConnectorManager.executeConnectionImportHandler(source);
    }

    public DatabaseConnectionRegistry getConnectionRegistryClassInstance(String databaseType) {
        return databaseConnectorManager.getConnectionRegistryClassInstance(databaseType);
    }

    @Override
    public <T extends AbstractConnection> T getConnection(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException{
        return databaseConnectorManager.getConnection(connectionId, databaseType);
    }

    @Override
    public <T extends AbstractConnection> String getConnectionAsString(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException{
        AbstractConnection connectionObj = databaseConnectorManager.getConnection(connectionId, databaseType);
        return connectionObj.makeConnectionData().getJson();
    }

    @Override
    public <T extends AbstractConnection> Map<String, T> getConnections(String databaseType) throws InstantiationException, IllegalAccessException{
        return databaseConnectorManager.getConnections(databaseType);
    }

    @Override
    public <T extends AbstractConnection, E extends ConnectionData> String getConnectionsAsString(String databaseType) throws InstantiationException, IllegalAccessException {
        String connections = null;
        Map<String, T> connectionsObj = databaseConnectorManager.getConnections(databaseType);
        if (connectionsObj != null) {
            List<E> connectionArray = new ArrayList<>();
            for (Map.Entry<String, T> entry : connectionsObj.entrySet()) {
                connectionArray.add((E)entry.getValue().makeConnectionData());
            }
            connections = new DbConnections(connectionArray).getJson();
        }
        return connections == null ? new JSONArray().toString() : connections;
    }

    @Override
    public boolean addEditConnection(AbstractConnection connection, Boolean isEdition) {
        return databaseConnectorManager.addEditConnection(connection, isEdition);
    }

    @Override
    public boolean testConnection(AbstractConnection connection) {
        return databaseConnectorManager.testConnection(connection);
    }

    @Override
    public boolean removeConnection(String connectionId, String databaseType) {
        return databaseConnectorManager.removeConnection(connectionId, databaseType);
    }

    @Override
    public boolean updateConnection(String connectionId, String databaseType, boolean connect) {
        return databaseConnectorManager.updateConnection(connectionId, databaseType, connect);
    }

    @Override
    public boolean isConnectionIdAvailable(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException {
        Map<String, AbstractConnection> connections = databaseConnectorManager.getConnections(databaseType);
        if (connections != null) {
            for (Map.Entry<String, AbstractConnection> entry : connections.entrySet()) {
                if (entry.getKey().equals(connectionId)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Map<String, Object> getServerStatus(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException {
        return databaseConnectorManager.getServerStatus(connectionId, databaseType);
    }

    @Override
    public void registerConnectorToRegistry(String connectionType, DatabaseConnectionRegistry databaseConnectionRegistry) {
        databaseConnectorManager.registerConnectorToRegistry(connectionType, databaseConnectionRegistry);
    }

    @Override
    public <T extends AbstractConnection> boolean hasConnection(String connectionId, String databaseType) throws InstantiationException, IllegalAccessException {
        return databaseConnectorManager.hasConnection(connectionId, databaseType);
    }

    @Override
    public String setPassword(Map<String, Object> map, String password) {
        return databaseConnectorManager.setPassword(map, password);
    }

    @Override
    public void deregisterConnectorFromRegistry(String connectionType) {
        databaseConnectorManager.deregisterConnectorFromRegistry(connectionType);
    }
}
