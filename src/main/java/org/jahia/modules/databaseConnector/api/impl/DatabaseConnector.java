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

import javax.jcr.RepositoryException;
import java.io.*;
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

    public String getConnection(String connectionId, DatabaseTypes databaseType) {
        String connection = null;
        switch (databaseType) {
            case MONGO:
                MongoConnection mongoConnection = databaseConnectorManager.getConnection(connectionId, databaseType);
                connection = mongoConnection.makeConnectionData().getJson();
                break;
            case REDIS:
                RedisConnection redisConnection = databaseConnectorManager.getConnection(connectionId, databaseType);
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
                if (mongoConnections != null) {
                    List<MongoConnectionData> mongoConnectionArray = new ArrayList<>();
                    for(Map.Entry<String, MongoConnection> entry : mongoConnections.entrySet()) {
                        mongoConnectionArray.add(entry.getValue().makeConnectionData());
                    }
                    connections = new DbConnections(mongoConnectionArray).getJson();
                }
                break;
            case REDIS:
                Map<String, RedisConnection> redisConnections = databaseConnectorManager.getRegisteredConnections(DatabaseTypes.REDIS);
                if (redisConnections != null) {
                    List<RedisConnectionData> redisConnectionArray = new ArrayList<>();
                    for (Map.Entry<String, RedisConnection> entry : redisConnections.entrySet()) {
                        redisConnectionArray.add(entry.getValue().makeConnectionData());
                    }
                    connections = new DbConnections(redisConnectionArray).getJson();
                }
                break;
        }
        return connections == null ? new JSONArray().toString() : connections;
    }

    public boolean addEditConnection(AbstractConnection connection, Boolean isEdition){
        return databaseConnectorManager.addEditConnection(connection, isEdition);
    }

    public boolean testConnection(AbstractConnection connection) {
        return databaseConnectorManager.testConnection(connection);
    }

    public boolean removeConnection(String connectionId, DatabaseTypes databaseType) {
        return databaseConnectorManager.removeConnection(connectionId, databaseType);
    }

    public boolean updateConnection(String connectionId, DatabaseTypes databaseType, boolean connect) {
        return databaseConnectorManager.updateConnection(connectionId, databaseType, connect);
    }

    public String getDatabaseTypes() {
        JSONArray databaseTypes = new JSONArray();
        for (DatabaseTypes databaseType: DatabaseTypes.getAllDatabaseTypes()) {
            databaseTypes.put(databaseType.name());
        }
        return databaseTypes.toString();
    }

    public boolean isConnectionIdAvailable(String connectionId, DatabaseTypes databaseType) {
        Map<String, AbstractConnection> connections = databaseConnectorManager.getRegisteredConnections(databaseType);
        if (connections != null) {
            for (Map.Entry<String, AbstractConnection> entry : connections.entrySet()) {
                if (entry.getKey().equals(connectionId)) {
                    return false;
                }
            }
        }
        return true;
    }


    public File exportConnections(JSONObject data)
            throws IOException, RepositoryException, JSONException {
        return databaseConnectorManager.exportConnections(data);
    }

    public Map importConnections(InputStream source) {
        return databaseConnectorManager.executeConnectionImportHandler(source);
    }
}
