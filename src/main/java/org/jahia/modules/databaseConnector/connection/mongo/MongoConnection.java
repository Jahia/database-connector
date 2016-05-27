package org.jahia.modules.databaseConnector.connection.mongo;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang.StringUtils;
import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.connection.DatabaseTypes;

import java.util.LinkedHashMap;

/**
 * Date: 11/1/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class MongoConnection extends AbstractConnection {

    public static final String NODE_TYPE = "dc:mongoConnection";

    public static final String WRITE_CONCERN_KEY = "dc:writeConcern";

    public static final String AUTH_DB_KEY = "dc:authDb";

    public static final String WRITE_CONCERN_DEFAULT_VALUE = "SAFE";

    private static final DatabaseTypes DATABASE_TYPE = DatabaseTypes.MONGO;

    private MongoDatabase databaseConnection;

    private MongoClient mongoClient;

    private String writeConcern;

    private String authDb;

    public MongoConnection(String id) {
        this.id = id;
        this.writeConcern = WRITE_CONCERN_DEFAULT_VALUE;
    }

    public MongoConnectionData makeConnectionData() {
        MongoConnectionData mongoConnectionData = new MongoConnectionData(id);
        mongoConnectionData.setHost(host);
        mongoConnectionData.setPort(port);
        mongoConnectionData.isConnected(isConnected);
        mongoConnectionData.setDbName(dbName);
        mongoConnectionData.setUser(user);
        mongoConnectionData.setPassword(password);
        mongoConnectionData.setWriteConcern(writeConcern);
        mongoConnectionData.setAuthDb(authDb);
        mongoConnectionData.setDatabaseType(DATABASE_TYPE);
        mongoConnectionData.setOptions(options);
        return mongoConnectionData;
    }

    @Override
    public void beforeUnregisterAsService() {
        mongoClient.close();
    }

    protected Object beforeRegisterAsService() {
        mongoClient = new MongoClient(new MongoClientURI(buildUri()));
        databaseConnection = mongoClient.getDatabase(dbName);
        return databaseConnection;
    }

    public DatabaseTypes getDatabaseType() {
        return DATABASE_TYPE;
    }
    
    @Override
    public String parseOptions(LinkedHashMap options) {
        //@TODO implement the structure of Mongo options object
        return null;
    }

    public String getWriteConcern() {
        return writeConcern;
    }

    public String getAuthDb() {
        return this.authDb;
    }

    public void setWriteConcern(String writeConcern) {
        this.writeConcern = writeConcern;
    }

    public void setAuthDb(String authDb) {
        this.authDb = authDb;
    }

    private String buildUri() {
        String uri = "mongodb://";
        if (!StringUtils.isEmpty(user)) {
            uri += user;
            if (!StringUtils.isEmpty(password)) {
                uri += ":" + password;
            }
            uri += "@";
        }
        uri += host;
        if (port != null) {
            uri += ":" + port;
        }

        uri += "/";

        if (!StringUtils.isEmpty(authDb)) {
            uri += authDb;
        }

        //If there are options or user, add query parameter start
        //@TODO add options array check when it is implemented
        if (!StringUtils.isEmpty(user)) {
            uri += "?";
        }

        //Options to be added:
        if (!StringUtils.isEmpty(user)) {
            uri += (!StringUtils.isEmpty(password) ? "authMechanism=SCRAM-SHA-1" : "authMechanism=GSSAPI");
        }
        return uri;
    }

    @Override
    public boolean testConnectionCreation() {
        try {
            mongoClient = new MongoClient(new MongoClientURI(buildUri(), MongoClientOptions.builder().serverSelectionTimeout(5000)));
            mongoClient.getServerAddressList();
        } catch (MongoTimeoutException ex) {
            //If the connection timeouts, the connection is invalid.
            return false;
        }
        return true;
    }
}
