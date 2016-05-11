package org.jahia.modules.databaseConnector.api.impl;

import org.jahia.modules.databaseConnector.connection.DatabaseConnectorManager;
import org.jahia.modules.databaseConnector.connection.DatabaseTypes;
import org.jahia.modules.databaseConnector.connection.mongo.MongoConnection;
import org.jahia.modules.databaseConnector.connection.mongo.MongoConnectionData;
import org.jahia.modules.databaseConnector.serialization.models.MongoDbConnections;
import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.services.content.JCRTemplate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author donnylam on 2016-05-04.
 */
public class DatabaseConnector extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(DatabaseConnector.class);

    private transient DatabaseConnectorManager databaseConnectorManager;

    public DatabaseConnector(JCRTemplate jcrTemplate, DatabaseConnectorManager databaseConnectorManager, Logger logger) {
        super(jcrTemplate, logger);
        this.databaseConnectorManager = databaseConnectorManager;
    }

    public String getConnection(String databaseId, DatabaseTypes databaseType) {
        MongoConnection mongoConnection = databaseConnectorManager.getConnection(databaseId, databaseType);
        return mongoConnection.makeConnectionData().getJson();
    }

    public String getConnections() throws JSONException{
        Map<String, MongoConnection> connections = databaseConnectorManager.getRegisteredConnections(DatabaseTypes.MONGO);
        List<MongoConnectionData> connectionArray = new ArrayList<>();
        for(Map.Entry<String, MongoConnection> entry : connections.entrySet()) {
            connectionArray.add(entry.getValue().makeConnectionData());
        }
        return new MongoDbConnections(connectionArray).getJson();
    }

    public boolean addEditConnection(String data, Boolean isEdition) throws JSONException, UnknownHostException{
        JSONObject connectionParameters = new JSONObject(data);
        String id = connectionParameters.has("id") ? connectionParameters.getString("id") : null;
        String host = connectionParameters.has("host") ? connectionParameters.getString("host") : null;
        Integer port = connectionParameters.has("port") ? connectionParameters.getInt("port") : null;
        String dbName = connectionParameters.has("dbName") ? connectionParameters.getString("dbName") : null;
        String uri = connectionParameters.has("uri") ? connectionParameters.getString("uri") : null;
        String user = connectionParameters.has("user") ? connectionParameters.getString("user") : null;
        String password = connectionParameters.has("password") ? connectionParameters.getString("password") : null;
        String writeConcern = connectionParameters.has("writeConcern") ? connectionParameters.getString("writeConcern") : null;
        AbstractConnection connection = new MongoConnection(id, host, port, dbName, user,
        password, writeConcern);
        return databaseConnectorManager.addEditConnection(connection, isEdition);
    }


    public boolean removeConnection(String databaseId, String databaseTypeName) {
        return databaseConnectorManager.removeConnection(databaseId, databaseTypeName);
    }

    public String getDatabaseTypes() {
        JSONArray databaseTypes = new JSONArray();
        for (DatabaseTypes databaseType: DatabaseTypes.getAllDatabaseTypes()) {
            databaseTypes.put(databaseType.name());
        }
        return databaseTypes.toString();
    }
}
