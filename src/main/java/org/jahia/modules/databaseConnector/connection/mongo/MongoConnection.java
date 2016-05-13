package org.jahia.modules.databaseConnector.connection.mongo;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.connection.DatabaseTypes;

import java.net.UnknownHostException;
import java.util.ArrayList;

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

    private final String writeConcern;

    private final String authDb;

    public MongoConnection(String id, String host, Integer port, Boolean isConnected) throws UnknownHostException {
        this(id, host, port, isConnected, null, null, null, null);
    }

    public MongoConnection(String id, String host, Integer port, Boolean isConnected, String dbName) throws UnknownHostException {
        this(id, host, port, isConnected, dbName, null, null, null);
    }

    public MongoConnection(String id, String host, Integer port, Boolean isConnected, String dbName, String user, String password) throws UnknownHostException {
        this(id, host, port, isConnected, dbName, user, password, null);
    }

    public MongoConnection(String id, String host, Integer port, Boolean isConnected, String dbName, String user, String password, String authDb) throws UnknownHostException {
        this(id, host, port, isConnected, dbName, user, password, authDb, null);
    }

    public MongoConnection(String id, String host, Integer port, Boolean isConnected, String dbName, String user, String password, String authDb, String writeConcern) throws UnknownHostException {
        super(id, host, port, isConnected, dbName, null, user, password , DATABASE_TYPE);
        if (writeConcern == null) {
            this.writeConcern = WRITE_CONCERN_DEFAULT_VALUE;
        }
        else {
            this.writeConcern = writeConcern;
        }
        this.authDb = authDb;
    }


    public MongoConnectionData makeConnectionData() {
        return new MongoConnectionData(id, host, port, isConnected, dbName, uri, user, password, authDb, writeConcern);
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

    public String getWriteConcern() {
        return writeConcern;
    }

    public String getAuthDb() {
        return this.authDb;
    }

    private String buildUri() {
        String uri = "mongodb://";
        if (user != null) {
            uri += user;
            if (password != null) {
                uri += ":" + password;
            }
            uri += "@";
        }
        uri += host;
        if (port != null) {
            uri += ":" + port;
        }

        uri += "/";

        if (authDb != null) {
            uri += authDb;
        }

        //If there are options or user, add query parameter start
        //@TODO add options array check when it is implemented
        if (user != null) {
            uri += "?";
        }

        //Options to be added:
        if (user != null) {
            uri += (password != null ? "authMechanism=SCRAM-SHA-1" : "authMechanism=GSSAPI");
        }
        return uri;
    }
}
