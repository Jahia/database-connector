package org.jahia.modules.databaseConnector.connection.mongo;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang.StringUtils;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.Document;
import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.connection.DatabaseTypes;
import org.jahia.utils.EncryptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static org.jahia.modules.databaseConnector.Utils.*;

/**
 * Date: 11/1/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class MongoConnection extends AbstractConnection {

    private static final Logger logger = LoggerFactory.getLogger(MongoConnection.class);

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
        BsonDocument serverStatusCommand = new BsonDocument()
                .append("serverStatus", new BsonInt32(1))
                .append("metrics", new BsonInt32(1))
                .append("locks", new BsonInt32(0))
                .append("dbStats", new BsonInt32(1))
                .append("collStats", new BsonInt32(1));
        if (!StringUtils.isEmpty(options)) {
            try {
                JSONObject jsonOptions = new JSONObject(options);
                if (jsonOptions.has("repl")) {
                    serverStatusCommand.append("repl", new BsonInt32(1));
                    return this.databaseConnection.runCommand(serverStatusCommand);
                }
            } catch (JSONException ex) {
                logger.error("Failed to parse connection options json", ex.getMessage());
                return null;
            }
        }
        serverStatusCommand.append("repl", new BsonInt32(0));
        Document serverStatus = this.databaseConnection.runCommand(serverStatusCommand);

        return serverStatus;
    }

    @Override
    public void beforeUnregisterAsService() {
        mongoClient.close();
    }

    protected Object beforeRegisterAsService() {
        mongoClient = new MongoClient(buildMongoClientUri(false));
        databaseConnection = mongoClient.getDatabase(dbName);
        return databaseConnection;
    }

    public DatabaseTypes getDatabaseType() {
        return DATABASE_TYPE;
    }
    
    @Override
    public String parseOptions(LinkedHashMap options) {
        JSONObject formattedOptions = new JSONObject();
        try {
            if (options.containsKey("connSettings")) {
                //Add connection settings
                formattedOptions.put("conn", options.get("connSettings"));
            }
            if (options.containsKey("connPoolSettings")) {
                //Add connection pool settings
                formattedOptions.put("connPool", options.get("connPoolSettings"));
            }
            if (options.containsKey("replicaSet")) {
                //Add replica settings
                LinkedHashMap repl = new LinkedHashMap();
                if (((LinkedHashMap) options.get("replicaSet")).containsKey("name")) {
                    repl.put("replicaSet", ((LinkedHashMap) options.get("replicaSet")).get("name"));
                }
                if (((LinkedHashMap) options.get("replicaSet")).containsKey("members")) {
                    LinkedList formattedMembers = new LinkedList();
                    List<String> members = (List) ((LinkedHashMap) options.get("replicaSet")).get("members");
                    for (String member : members) {
                        LinkedHashMap formattedMember = new LinkedHashMap();
                        if (member.contains(":")) {
                            formattedMember.put("host", member.substring(0, member.indexOf(":")));
                            formattedMember.put("port", member.substring(member.indexOf(":") + 1, member.length()));
                        } else {
                            formattedMember.put("host", member);
                        }
                        formattedMembers.push(formattedMember);
                    }
                    repl.put("members", formattedMembers);
                }
                formattedOptions.put("repl", repl);
            }
        } catch(JSONException ex) {
            logger.error("Failed to serialize imported connection options", ex.getMessage());
        }
        return formattedOptions.toString();
    }

    @Override
    public String getSerializedExportData() {
        StringBuilder serializedString = new StringBuilder();
        serializedString.append(
                TABU + "type " + DOUBLE_QUOTE + DATABASE_TYPE + DOUBLE_QUOTE + NEW_LINE +
                TABU + "host " + DOUBLE_QUOTE + this.host + DOUBLE_QUOTE + NEW_LINE +
                TABU + "dbName " + DOUBLE_QUOTE + this.dbName + DOUBLE_QUOTE + NEW_LINE +
                TABU + "identifier " + DOUBLE_QUOTE + this.id + DOUBLE_QUOTE + NEW_LINE +
                TABU + "isConnected " + DOUBLE_QUOTE + this.isConnected() + DOUBLE_QUOTE + NEW_LINE
        );

        if (this.port != null) {
            serializedString.append(TABU + "port " + DOUBLE_QUOTE + this.port + DOUBLE_QUOTE + NEW_LINE);
        }

        if (!StringUtils.isEmpty(this.authDb)) {
            serializedString.append(TABU + "authDb " + DOUBLE_QUOTE + this.authDb + DOUBLE_QUOTE + NEW_LINE);
        }

        if (!StringUtils.isEmpty(this.user)) {
            serializedString.append(TABU + "user " + DOUBLE_QUOTE + this.user + DOUBLE_QUOTE + NEW_LINE);
        }

        if (!StringUtils.isEmpty(this.password)) {
            serializedString.append(TABU + "password " + DOUBLE_QUOTE + EncryptionUtils.passwordBaseEncrypt(this.password) + "_ENC" + DOUBLE_QUOTE + NEW_LINE);
        }

        if (this.options != null) {
            try {
                JSONObject jsonOptions = new JSONObject(this.options);
                serializedString.append(TABU + "options {");
                //Handle connection pool settings
                if (jsonOptions.has("connPool")) {
                    JSONObject jsonConnPool = new JSONObject(jsonOptions.get("connPoolSettings"));
                    serializedString.append(NEW_LINE + TABU + TABU + "connPool {");
                    if (jsonConnPool.has("minPoolSize") && !StringUtils.isEmpty(jsonConnPool.getString("minPoolSize"))) {
                        serializedString.append(NEW_LINE + TABU + TABU + TABU + "minPoolSize " + DOUBLE_QUOTE + jsonConnPool.getString("minPoolSize") + DOUBLE_QUOTE);
                    }
                    if (jsonConnPool.has("maxPoolSize") && !StringUtils.isEmpty(jsonConnPool.getString("maxPoolSize"))) {
                        serializedString.append(NEW_LINE + TABU + TABU + TABU + "maxPoolSize " + DOUBLE_QUOTE + jsonConnPool.getString("maxPoolSize") + DOUBLE_QUOTE);
                    }
                    if (jsonConnPool.has("waitQueueTimeoutMS") && !StringUtils.isEmpty(jsonConnPool.getString("waitQueueTimeoutMS"))) {
                        serializedString.append(NEW_LINE + TABU + TABU + TABU + "waitQueueTimeoutMS " + DOUBLE_QUOTE + jsonConnPool.getString("waitQueueTimeoutMS") + DOUBLE_QUOTE);
                    }
                    serializedString.append(NEW_LINE + TABU + TABU + "}");
                }
                //Handle connection settings
                if (jsonOptions.has("conn")) {
                    JSONObject jsonConn = new JSONObject(jsonOptions.get("conn"));
                    serializedString.append(NEW_LINE + TABU + TABU + "connSettings {");
                    if (jsonConn.has("connectTimeoutMS") && !StringUtils.isEmpty(jsonConn.getString("connectTimeoutMS"))) {
                        serializedString.append(NEW_LINE + TABU + TABU + TABU + "connectTimeoutMS " + DOUBLE_QUOTE + jsonConn.getInt("connectTimeoutMS") + DOUBLE_QUOTE);
                    }
                    if (jsonConn.has("socketTimeoutMS") && !StringUtils.isEmpty(jsonConn.getString("socketTimeoutMS"))) {
                        serializedString.append(NEW_LINE + TABU + TABU + TABU + "socketTimeoutMS " + DOUBLE_QUOTE + jsonConn.getInt("socketTimeoutMS") + DOUBLE_QUOTE);
                    }
                    serializedString.append(NEW_LINE + TABU + TABU + "}");
                }
                //Handle replicate set options
                if (jsonOptions.has("repl")) {
                    JSONObject jsonRepl = jsonOptions.getJSONObject("repl");
                    serializedString.append(NEW_LINE + TABU + TABU + "replicaSet {");
                    if (jsonRepl.has("replicaSet") && !StringUtils.isEmpty(jsonRepl.getString("replicaSet"))) {
                        serializedString.append(NEW_LINE + TABU + TABU + TABU + "name " + DOUBLE_QUOTE + jsonRepl.getString("replicaSet") + DOUBLE_QUOTE);
                    }
                    JSONArray members = jsonRepl.getJSONArray("members");
                    serializedString.append(NEW_LINE + TABU + TABU + TABU + "members ");
                    for (int i = 0; i < members.length(); i++) {
                        if (i != 0) {
                            serializedString.append(", ");
                        }
                        JSONObject member = members.getJSONObject(i);
                        serializedString.append(DOUBLE_QUOTE + member.getString("host") + (member.has("port") && !StringUtils.isEmpty(member.getString("port")) ? ":" + member.getString("port"): "") + DOUBLE_QUOTE);
                    }
                    serializedString.append(NEW_LINE + TABU + TABU + "}");
                }
                serializedString.append(NEW_LINE + TABU + "}");
            } catch (JSONException ex) {
                logger.error("Failed to parse connection options json", ex.getMessage());
            }
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

    private MongoClientURI buildMongoClientUri(boolean isTest) {
        if (!StringUtils.isEmpty(options)) {
            if (isTest) {
                return new MongoClientURI(buildUri(), buildMongoClientOptions().serverSelectionTimeout(5000));
            } else {
                return new MongoClientURI(buildUri(), buildMongoClientOptions());
            }
        } else {
            if (isTest) {
                return new MongoClientURI(buildUri(), MongoClientOptions.builder().serverSelectionTimeout(5000));

            } else {
                return new MongoClientURI(buildUri());
            }
        }
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
        //Check if it is a replica set.
        if (!StringUtils.isEmpty(options)) {
            try {
                JSONObject jsonOptions = new JSONObject(options);
                if (jsonOptions.has("repl")) {
                    JSONArray members = jsonOptions.getJSONObject("repl").getJSONArray("members");
                    for (int i = 0; i < members.length(); i++) {
                        JSONObject member = members.getJSONObject(i);
                        //concatenate all the address separated by commas, representing the replica set members
                        uri += "," + member.get("host");
                        if (member.has("port") && !StringUtils.isEmpty(member.getString("port"))) {
                            uri += ":" + member.getString("port");
                        }
                    }
                }
            } catch (JSONException ex) {
                logger.error("Failed to parse connection options json", ex.getMessage());
            }
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

        if (!StringUtils.isEmpty(user)) {
            uri += (!StringUtils.isEmpty(password) ? "authMechanism=SCRAM-SHA-1" : "authMechanism=GSSAPI");
        }
        return uri;
    }

    private MongoClientOptions.Builder buildMongoClientOptions() {
        try {
            JSONObject jsonOptions = new JSONObject(options);
            MongoClientOptions.Builder builder = MongoClientOptions.builder();
            //Handle replicate set options
            if (jsonOptions.has("repl")) {
                JSONObject jsonRepl = jsonOptions.getJSONObject("repl");
                if(jsonRepl.has("replicaSet") && !StringUtils.isEmpty(jsonRepl.getString("replicaSet"))) {
                    builder.requiredReplicaSetName(jsonRepl.getString("replicaSet"));
                } else {
                    //If there is no replica set name then we throw an exception.
                    throw new MongoClientException("No replica set name found");
                }
            }
            //Handle connection pool settings
            if (jsonOptions.has("connPool")) {
                JSONObject jsonConnPool = new JSONObject(jsonOptions.get("connPool"));
                if (jsonConnPool.has("minPoolSize") && !StringUtils.isEmpty(jsonConnPool.getString("minPoolSize"))) {
                    builder.minConnectionsPerHost(jsonConnPool.getInt("minPoolSize"));
                }
                if (jsonConnPool.has("maxPoolSize") && !StringUtils.isEmpty(jsonConnPool.getString("maxPoolSize"))) {
                    builder.connectionsPerHost(jsonConnPool.getInt("maxPoolSize"));
                }
                if (jsonConnPool.has("waitQueueTimeoutMS") && !StringUtils.isEmpty(jsonConnPool.getString("waitQueueTimeoutMS"))) {
                    builder.maxWaitTime(jsonConnPool.getInt("waitQueueTimeoutMS"));
                }
            }
            //Handle connection settings
            if (jsonOptions.has("conn")) {
                JSONObject jsonConn = new JSONObject(jsonOptions.get("conn"));
                if (jsonConn.has("connectTimeoutMS") && !StringUtils.isEmpty(jsonConn.getString("connectTimeoutMS"))) {
                    builder.connectTimeout(jsonConn.getInt("connectTimeoutMS"));
                }
                if (jsonConn.has("socketTimeoutMS") && !StringUtils.isEmpty(jsonConn.getString("socketTimeoutMS"))) {
                    builder.connectTimeout(jsonConn.getInt("socketTimeoutMS"));
                }
            }
            return builder;
        } catch (JSONException ex) {
            logger.error("Failed to parse connection options json", ex.getMessage());
            return null;
        } catch (MongoClientException ex) {
            logger.error("Failed to build Mongo client options", ex.getMessage());
            return null;
        }
    }

    @Override
    public boolean testConnectionCreation() {
        try {
            mongoClient = new MongoClient(buildMongoClientUri(true));
            mongoClient.getServerAddressList();
        } catch (MongoTimeoutException ex) {
            //If the connection timeouts, the connection is invalid.
            return false;
        }
        return true;
    }
}
