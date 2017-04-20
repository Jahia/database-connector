package org.jahia.modules.databaseConnector.connection.mongo;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang.StringUtils;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.utils.EncryptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static org.jahia.modules.databaseConnector.util.Utils.*;

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
    public static final String WRITE_CONCERN_DEFAULT_VALUE = "ACKNOWLEDGED";
    public static final Integer DEFAULT_PORT = 27017;

    private static final Logger logger = LoggerFactory.getLogger(MongoConnection.class);
    public static final String DATABASE_TYPE = "Mongo";
    public static final String DISPLAY_NAME = "MongoDB";
    private static List WRITE_CONCERN_OPTIONS = null;
    private MongoDatabase databaseConnection;

    private MongoClient mongoClient;

    private String writeConcern;

    private String authDb;

    public MongoConnection(String id) {
        this.id = id;
        this.writeConcern = WRITE_CONCERN_DEFAULT_VALUE;
    }

    public static List<String> getWriteConcernOptions() {
        if (WRITE_CONCERN_OPTIONS == null) {
            WRITE_CONCERN_OPTIONS = new LinkedList<>();
            WRITE_CONCERN_OPTIONS.add("ACKNOWLEDGED");
            WRITE_CONCERN_OPTIONS.add("UNACKNOWLEDGED");
            WRITE_CONCERN_OPTIONS.add("JOURNALED");
            WRITE_CONCERN_OPTIONS.add("MAJORITY");
            WRITE_CONCERN_OPTIONS.add("W1");
            WRITE_CONCERN_OPTIONS.add("W2");
            WRITE_CONCERN_OPTIONS.add("W3");
        }
        return WRITE_CONCERN_OPTIONS;
    }

    public MongoConnectionData makeConnectionData() {
        MongoConnectionData mongoConnectionData = new MongoConnectionData(id);
        mongoConnectionData.setHost(host);
        mongoConnectionData.setPort(port == null ? DEFAULT_PORT : port);
        mongoConnectionData.isConnected(isConnected);
        mongoConnectionData.setDbName(dbName);
        mongoConnectionData.setUser(user);
        mongoConnectionData.setPassword(password);
        mongoConnectionData.setWriteConcern(writeConcern);
        mongoConnectionData.setAuthDb(authDb);
        mongoConnectionData.setDatabaseType(DATABASE_TYPE);
        mongoConnectionData.setDisplayName(DISPLAY_NAME);
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

        return this.databaseConnection.runCommand(serverStatusCommand);
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

    public String getDatabaseType() {
        return DATABASE_TYPE;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
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
                    if (((LinkedHashMap) options.get("replicaSet")).get("members") instanceof String) {
                        String member = (String) ((LinkedHashMap) options.get("replicaSet")).get("members");
                        LinkedHashMap formattedMember = new LinkedHashMap();
                        if (member.contains(":")) {
                            formattedMember.put("host", member.substring(0, member.indexOf(":")));
                            formattedMember.put("port", member.substring(member.indexOf(":") + 1, member.length()));
                        } else {
                            formattedMember.put("host", member);
                        }
                        formattedMembers.push(formattedMember);
                    } else if (((LinkedHashMap) options.get("replicaSet")).get("members") instanceof List) {
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
                    }
                    repl.put("members", formattedMembers);
                }
                formattedOptions.put("repl", repl);
            }
        } catch (JSONException ex) {
            logger.error("Failed to serialize imported connection options", ex.getMessage());
        }
        return formattedOptions.toString();
    }

    @Override
    public String getSerializedExportData() {
        StringBuilder serializedString = new StringBuilder();
        serializedString.append(TABU).append("type ").append(DOUBLE_QUOTE).append(DATABASE_TYPE).append(DOUBLE_QUOTE).append(NEW_LINE);
        serializedString.append(TABU).append("host ").append(DOUBLE_QUOTE).append(this.host).append(DOUBLE_QUOTE).append(NEW_LINE);
        serializedString.append(TABU).append("dbName ").append(DOUBLE_QUOTE).append(this.dbName).append(DOUBLE_QUOTE).append(NEW_LINE);
        serializedString.append(TABU).append("identifier ").append(DOUBLE_QUOTE).append(this.id).append(DOUBLE_QUOTE).append(NEW_LINE);
        serializedString.append(TABU).append("isConnected ").append(DOUBLE_QUOTE).append(this.isConnected()).append(DOUBLE_QUOTE).append(NEW_LINE);
        serializedString.append(TABU).append("writeConcern ").append(DOUBLE_QUOTE).append(this.writeConcern).append(DOUBLE_QUOTE).append(NEW_LINE);
        serializedString.append(TABU + "port " + DOUBLE_QUOTE).append(this.port != null ? this.port : DEFAULT_PORT).append(DOUBLE_QUOTE).append(NEW_LINE);

        if (!StringUtils.isEmpty(this.authDb)) {
            serializedString.append(TABU + "authDb " + DOUBLE_QUOTE).append(this.authDb).append(DOUBLE_QUOTE).append(NEW_LINE);
        }

        if (!StringUtils.isEmpty(this.user)) {
            serializedString.append(TABU + "user " + DOUBLE_QUOTE).append(this.user).append(DOUBLE_QUOTE).append(NEW_LINE);
        }

        if (!StringUtils.isEmpty(this.password)) {
            serializedString.append(TABU + "password " + DOUBLE_QUOTE).append(EncryptionUtils.passwordBaseEncrypt(this.password)).append("_ENC").append(DOUBLE_QUOTE).append(NEW_LINE);
        }

        if (this.options != null) {
            try {
                JSONObject jsonOptions = new JSONObject(this.options);
                serializedString.append(TABU + "options {");
                //Handle connection pool settings
                if (jsonOptions.has("connPool")) {
                    JSONObject jsonConnPool = jsonOptions.getJSONObject("connPool");
                    serializedString.append(NEW_LINE).append(TABU).append(TABU).append("connPoolSettings {");
                    if (jsonConnPool.has("minPoolSize") && !StringUtils.isEmpty(jsonConnPool.getString("minPoolSize"))) {
                        serializedString.append(NEW_LINE).append(TABU).append(TABU).append(TABU).append("minPoolSize ").append(DOUBLE_QUOTE).append(jsonConnPool.getString("minPoolSize")).append(DOUBLE_QUOTE);
                    }
                    if (jsonConnPool.has("maxPoolSize") && !StringUtils.isEmpty(jsonConnPool.getString("maxPoolSize"))) {
                        serializedString.append(NEW_LINE).append(TABU).append(TABU).append(TABU).append("maxPoolSize ").append(DOUBLE_QUOTE).append(jsonConnPool.getString("maxPoolSize")).append(DOUBLE_QUOTE);
                    }
                    if (jsonConnPool.has("waitQueueTimeoutMS") && !StringUtils.isEmpty(jsonConnPool.getString("waitQueueTimeoutMS"))) {
                        serializedString.append(NEW_LINE).append(TABU).append(TABU).append(TABU).append("waitQueueTimeoutMS ").append(DOUBLE_QUOTE).append(jsonConnPool.getString("waitQueueTimeoutMS")).append(DOUBLE_QUOTE);
                    }
                    serializedString.append(NEW_LINE).append(TABU).append(TABU).append("}");
                }
                //Handle connection settings
                if (jsonOptions.has("conn")) {
                    JSONObject jsonConn = jsonOptions.getJSONObject("conn");
                    serializedString.append(NEW_LINE).append(TABU).append(TABU).append("connSettings {");
                    if (jsonConn.has("connectTimeoutMS") && !StringUtils.isEmpty(jsonConn.getString("connectTimeoutMS"))) {
                        serializedString.append(NEW_LINE).append(TABU).append(TABU).append(TABU).append("connectTimeoutMS ").append(DOUBLE_QUOTE).append(jsonConn.getInt("connectTimeoutMS")).append(DOUBLE_QUOTE);
                    }
                    if (jsonConn.has("socketTimeoutMS") && !StringUtils.isEmpty(jsonConn.getString("socketTimeoutMS"))) {
                        serializedString.append(NEW_LINE).append(TABU).append(TABU).append(TABU).append("socketTimeoutMS ").append(DOUBLE_QUOTE).append(jsonConn.getInt("socketTimeoutMS")).append(DOUBLE_QUOTE);
                    }
                    serializedString.append(NEW_LINE).append(TABU).append(TABU).append("}");
                }
                //Handle replicate set options
                if (jsonOptions.has("repl")) {
                    JSONObject jsonRepl = jsonOptions.getJSONObject("repl");
                    serializedString.append(NEW_LINE).append(TABU).append(TABU).append("replicaSet {");
                    if (jsonRepl.has("replicaSet") && !StringUtils.isEmpty(jsonRepl.getString("replicaSet"))) {
                        serializedString.append(NEW_LINE).append(TABU).append(TABU).append(TABU).append("name ").append(DOUBLE_QUOTE).append(jsonRepl.getString("replicaSet")).append(DOUBLE_QUOTE);
                    }
                    JSONArray members = jsonRepl.getJSONArray("members");
                    serializedString.append(NEW_LINE).append(TABU).append(TABU).append(TABU).append("members ");
                    for (int i = 0; i < members.length(); i++) {
                        if (i != 0) {
                            serializedString.append(", ");
                        }
                        JSONObject member = members.getJSONObject(i);
                        serializedString.append(DOUBLE_QUOTE).append(member.getString("host")).append(member.has("port") && !StringUtils.isEmpty(member.getString("port")) ? ":" + member.getString("port") : "").append(DOUBLE_QUOTE);
                    }
                    serializedString.append(NEW_LINE).append(TABU).append(TABU).append("}");
                }
                serializedString.append(NEW_LINE).append(TABU).append("}");
            } catch (JSONException ex) {
                logger.error("Failed to parse connection options json", ex.getMessage());
            }
        }
        return serializedString.toString();
    }

    public String getWriteConcern() {
        return writeConcern;
    }

    public void setWriteConcern(String writeConcern) {
        this.writeConcern = writeConcern;
    }

    public String getAuthDb() {
        return this.authDb;
    }

    public void setAuthDb(String authDb) {
        this.authDb = authDb;
    }

    private MongoClientURI buildMongoClientUri(boolean isTest) {
        MongoClientOptions.Builder builder = MongoClientOptions.builder();
        //Set the write concern
        builder.writeConcern(WriteConcern.valueOf(writeConcern));
        if (!StringUtils.isEmpty(options)) {
            builder = buildMongoClientOptions(builder);
        }
        if (isTest) {
            builder.serverSelectionTimeout(5000);
        }
        return new MongoClientURI(buildUri(), builder);
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

    private MongoClientOptions.Builder buildMongoClientOptions(MongoClientOptions.Builder builder) {
        try {
            JSONObject jsonOptions = new JSONObject(options);
            //Handle replicate set options
            if (jsonOptions.has("repl")) {
                JSONObject jsonRepl = jsonOptions.getJSONObject("repl");
                if (jsonRepl.has("replicaSet") && !StringUtils.isEmpty(jsonRepl.getString("replicaSet"))) {
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
