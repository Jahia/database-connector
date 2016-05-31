package org.jahia.modules.databaseConnector.connection.mongo;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang.StringUtils;
import org.jahia.modules.databaseConnector.Utils;
import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.connection.DatabaseTypes;


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


        //Implement the exported content data
        StringBuilder serializedString = new StringBuilder();
        serializedString.append( NEW_LINE + " connection {" + NEW_LINE + TABU + "type " + DATABASE_TYPE + Utils.NEW_LINE +
                TABU +"host " + this.host + NEW_LINE + TABU + "port " + this.port + NEW_LINE + TABU +
                "dbName" + this.dbName + NEW_LINE + TABU + "identifier " + this.id );
        if (this.options != null) {
            serializedString.append( NEW_LINE + TABU + "options " + "{ "+ this.options + NEW_LINE + TABU  + "}");
        }
        serializedString.append(NEW_LINE + TABU + "user " + this.user + NEW_LINE + TABU +
                "password " + this.password + NEW_LINE + TABU + "authDb " + this.authDb + NEW_LINE + "}");
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
