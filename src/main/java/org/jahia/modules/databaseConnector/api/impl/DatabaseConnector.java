package org.jahia.modules.databaseConnector.api.impl;

import org.jahia.modules.databaseConnector.ConnectionData;
import org.jahia.modules.databaseConnector.DatabaseConnectorManager;
import org.jahia.modules.databaseConnector.DatabaseTypes;
import org.jahia.modules.databaseConnector.mongo.MongoConnectionDataImpl;
import org.jahia.modules.databaseConnector.webflow.model.Connection;
import org.jahia.modules.databaseConnector.webflow.model.ConnectionFactory;
import org.jahia.modules.databaseConnector.webflow.model.mongo.MongoConnectionImpl;
import org.jahia.services.content.JCRTemplate;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

/**
 * Created by donnylam on 2016-05-04.
 */
public class DatabaseConnector extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(DatabaseConnector.class);

    private transient DatabaseConnectorManager databaseConnectorManager;

    public DatabaseConnector(JCRTemplate jcrTemplate, DatabaseConnectorManager databaseConnectorManager, Logger logger) {
        super(jcrTemplate, logger);
        this.databaseConnectorManager = databaseConnectorManager;
    }

    public Connection initConnection(String databaseTypeName) {
        Connection connection = ConnectionFactory.makeConnection(databaseTypeName);
        String id = databaseConnectorManager.getNextAvailableId(connection.getDatabaseType());
        connection.setId(id);
        return connection;
    }


    public Connection getConnection(String databaseId, String databaseTypeName) {
        return ConnectionFactory.makeConnection(databaseConnectorManager.getConnectionData(databaseId, databaseTypeName));
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
        ConnectionData connectionData = new MongoConnectionDataImpl(
                id,
                host,
                port,
                dbName,
                uri,
                user,
                password,
                DatabaseTypes.MONGO,
                writeConcern);
        Connection connection = new MongoConnectionImpl(connectionData);
        return databaseConnectorManager.addEditConnection(connection, isEdition);
    }


    public boolean removeConnection(String databaseId, String databaseTypeName) {
        return databaseConnectorManager.removeConnection(databaseId, databaseTypeName);
    }
}
