package org.jahia.modules.databaseConnector.connection.redis;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.*;
import org.apache.commons.lang.StringUtils;
import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.connection.DatabaseTypes;
import org.jahia.utils.EncryptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static org.jahia.modules.databaseConnector.Utils.DOUBLE_QUOTE;
import static org.jahia.modules.databaseConnector.Utils.NEW_LINE;
import static org.jahia.modules.databaseConnector.Utils.TABU;

/**
 * @author by stefan on 2016-05-11.
 */
public class RedisConnection extends AbstractConnection {

    private RedisClient redisClient;

    public static final String NODE_TYPE = "dc:redisConnection";

    private Integer timeout  ;

    private Integer weight ;

    public static final String TIMEOUT_KEY = "dc:timeout";

    public static final String WEIGHT_KEY = "dc:weight";

    private static final DatabaseTypes DATABASE_TYPE = DatabaseTypes.REDIS;

    private static final Integer DEFAULT_PORT = new Integer(6379);

    public RedisConnection(String id) {
        this.id = id;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getWeight() {
        return weight;
    }

//    @Override
    public RedisConnectionData makeConnectionData() {
        RedisConnectionData redisConnectionData = new RedisConnectionData(id);
        redisConnectionData.setHost(host);
        redisConnectionData.setPort(port == null ? DEFAULT_PORT : port);
        redisConnectionData.isConnected(isConnected);
        redisConnectionData.setDbName(dbName);
        redisConnectionData.setUser(user);
        redisConnectionData.setPassword(password);
        redisConnectionData.setTimeout(timeout);
        redisConnectionData.setWeight(weight);
        redisConnectionData.setDatabaseType(DATABASE_TYPE);
        return redisConnectionData;
    }

    @Override
    public Object getServerStatus() {
        return null;
    }

    @Override
    protected Object beforeRegisterAsService() {
//        redisClient = new Jedis(buildRedisClientUri());
//        redisClient.get("identifier");
//                databaseConnection = redisClient.getDB();
//        return databaseConnection;
//
//        String uri = buildRedisClientUri();
//        redisClient = JedisURIHelper.getDBIndex(URI uri);
       return null;
    }


    @Override
    public void beforeUnregisterAsService() {
        redisClient.close();
    }

    @Override
    public boolean testConnectionCreation() {
        return false;
    }

    @Override
    public String parseOptions(LinkedHashMap options) {
        //@TODO Implement the parsing of options.
        return null;
    }




    private String buildRedisClientUri() {
        String uri = "redisdb://";
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

        //@TODO add options array check when it is implemented
        if (!StringUtils.isEmpty(user)) {
            uri += "?";
        }

        if (!StringUtils.isEmpty(user)) {
            uri += (!StringUtils.isEmpty(password) ? "authMechanism=SCRAM-SHA-1" : "authMechanism=GSSAPI");
        }
        return uri;
    }

    public DatabaseTypes getDatabaseType() {
        return DATABASE_TYPE;
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


        if (!StringUtils.isEmpty(this.password)) {
            serializedString.append(TABU + "password " + DOUBLE_QUOTE + EncryptionUtils.passwordBaseEncrypt(this.password) + "_ENC" + DOUBLE_QUOTE + NEW_LINE);
        }

        if (this.options != null) {
            try {
                JSONObject jsonOptions = new JSONObject(this.options);

            } catch (JSONException ex) {
                logger.error("Failed to parse connection options json", ex.getMessage());
            }
        }
        return serializedString.toString();
    }



}