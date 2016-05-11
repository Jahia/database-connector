package org.jahia.modules.databaseConnector.api.impl;

import org.jahia.modules.databaseConnector.connection.ConnectionData;
import org.jahia.modules.databaseConnector.connection.DatabaseConnectorManager;
import org.jahia.modules.databaseConnector.connection.DatabaseTypes;
import org.jahia.modules.databaseConnector.connection.mongo.MongoConnection;
import org.jahia.modules.databaseConnector.connection.mongo.MongoConnectionData;
import org.jahia.modules.databaseConnector.connection.redis.RedisConnection;
import org.jahia.modules.databaseConnector.connection.redis.RedisConnectionData;
import org.jahia.modules.databaseConnector.serialization.models.DbConnections;
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
        String connection = null;
        switch (databaseType) {
            case MONGO:
                MongoConnection mongoConnection = databaseConnectorManager.getConnection(databaseId, databaseType);
                connection = mongoConnection.makeConnectionData().getJson();
                break;
            case REDIS:
                RedisConnection redisConnection = databaseConnectorManager.getConnection(databaseId, databaseType);
                connection = redisConnection.makeConnectionData().getJson();
                break;
        }
        return connection;
    }

    public <T extends ConnectionData> String getConnections(DatabaseTypes databaseType) throws JSONException{
        String connections = null;
        switch (databaseType) {
            case MONGO:
                Map<String, MongoConnection> mongoConnections = databaseConnectorManager.getRegisteredConnections(DatabaseTypes.MONGO);
                List<MongoConnectionData> mongoConnectionArray = new ArrayList<>();
                for(Map.Entry<String, MongoConnection> entry : mongoConnections.entrySet()) {
                    mongoConnectionArray.add(entry.getValue().makeConnectionData());
                }
                connections = new DbConnections(mongoConnectionArray).getJson();
                break;
            case REDIS:
                Map<String, RedisConnection> redisConnections = databaseConnectorManager.getRegisteredConnections(DatabaseTypes.REDIS);
                List<RedisConnectionData> redisConnectionArray = new ArrayList<>();
                for(Map.Entry<String, RedisConnection> entry : redisConnections.entrySet()) {
                    redisConnectionArray.add(entry.getValue().makeConnectionData());
                }
                connections = new DbConnections(redisConnectionArray).getJson();
                break;
        }
        return connections;
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
