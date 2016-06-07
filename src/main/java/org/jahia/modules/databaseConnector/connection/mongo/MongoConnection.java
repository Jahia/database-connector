package org.jahia.modules.databaseConnector.connection.mongo;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang.StringUtils;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.Document;
import org.jahia.modules.databaseConnector.Utils;
import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.connection.ConnectionData;
import org.jahia.modules.databaseConnector.connection.DatabaseTypes;
import org.jahia.utils.EncryptionUtils;

import java.util.LinkedHashMap;

import static org.jahia.modules.databaseConnector.Utils.*;

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
    public Object getServerStatus() {
        Document serverStatus = this.databaseConnection.runCommand(new BsonDocument()
                .append("serverStatus", new BsonInt32(1))
                .append("repl", new BsonInt32(0))
                .append("metrics", new BsonInt32(1))
                .append("locks", new BsonInt32(0))
                .append("dbStats", new BsonInt32(1))
                .append("collStats", new BsonInt32(1))
        );
        return serverStatus;
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

    @Override
    public String getSerializedExportData() {
        StringBuilder serializedString = new StringBuilder();
        serializedString.append(
                TABU + "type " + DOUBLE_QUOTE + DATABASE_TYPE + DOUBLE_QUOTE + NEW_LINE +
                TABU +"host " + DOUBLE_QUOTE + this.host + DOUBLE_QUOTE + NEW_LINE +
                TABU + "port " + DOUBLE_QUOTE + this.port + DOUBLE_QUOTE + NEW_LINE +
                TABU + "dbName" + DOUBLE_QUOTE + this.dbName + DOUBLE_QUOTE + NEW_LINE +
                TABU + "identifier " + DOUBLE_QUOTE + this.id + DOUBLE_QUOTE + NEW_LINE +
                TABU + "isConnected " + DOUBLE_QUOTE + this.isConnected() + DOUBLE_QUOTE + NEW_LINE +
                TABU + "authDb " + DOUBLE_QUOTE + this.authDb + DOUBLE_QUOTE + NEW_LINE +
                TABU + "user " + DOUBLE_QUOTE + this.user + DOUBLE_QUOTE + NEW_LINE
        );

        if (!StringUtils.isEmpty(this.password)) {
            serializedString.append(TABU + "password " + DOUBLE_QUOTE + EncryptionUtils.passwordBaseEncrypt(this.password) + "_ENC" + DOUBLE_QUOTE );
        }


        if (this.options != null) {
            //@TODO implement options structure
            serializedString.append(NEW_LINE + TABU + "options " + "{" + NEW_LINE + TABU + "}");
        }
        return serializedString.toString();
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
