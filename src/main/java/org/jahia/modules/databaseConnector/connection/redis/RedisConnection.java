package org.jahia.modules.databaseConnector.connection.redis;

import com.lambdaworks.redis.*;
import org.apache.commons.lang.StringUtils;
import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.connection.DatabaseTypes;
import org.jahia.utils.EncryptionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;


import static org.jahia.modules.databaseConnector.Utils.*;

/**
 * @author by stefan on 2016-05-11.
 */
public class RedisConnection extends AbstractConnection {

    private static final Logger logger = LoggerFactory.getLogger(RedisConnection.class);

    private RedisClient redisClient;

    public static final String NODE_TYPE = "dc:redisConnection";

    private Long timeout;

    private Integer weight;

    public static final String TIMEOUT_KEY = "dc:timeout";

    public static final String WEIGHT_KEY = "dc:weight";

    private static final DatabaseTypes DATABASE_TYPE = DatabaseTypes.REDIS;

    private static final Integer DEFAULT_PORT = new Integer(6379);

    public RedisConnection(String id) {
        this.id = id;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getWeight() {
        return weight;
    }

    public RedisConnectionData makeConnectionData() {
        RedisConnectionData redisConnectionData = new RedisConnectionData(id);
        redisConnectionData.setHost(host);
        redisConnectionData.setPort(port == null ? DEFAULT_PORT : port);
        redisConnectionData.isConnected(isConnected);
        redisConnectionData.setDbName(dbName);
        redisConnectionData.setPassword(password);
        redisConnectionData.setTimeout(timeout);
        redisConnectionData.setWeight(weight);
        redisConnectionData.setDatabaseType(DATABASE_TYPE);
        return redisConnectionData;
    }

    @Override
    // @TODO mod-1164 retrieve metrics for redis
    public Object getServerStatus() {
        return null;
    }

    @Override
    protected Object beforeRegisterAsService() {

        redisClient = RedisClient.create(buildRedisClientUri());

        return redisClient.connect();
    }

    @Override
    public void beforeUnregisterAsService() {

        redisClient.shutdown();
    }

    @Override
    public boolean testConnectionCreation() {
        try {
            RedisClient redisClient = RedisClient.create(buildRedisClientUri());
             redisClient.connect();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public String parseOptions(LinkedHashMap options) {
        //@TODO Implement the parsing of options.
        return null;
    }


    private RedisURI buildRedisClientUri() {
//        redis :// [password@] host [: port] [/ database] [? [timeout=timeout[d|h|m|s|ms|us|ns]] [&database=database]]

        RedisURI.Builder builder = RedisURI.Builder.redis(host, port);
        if (password != null) {
            builder.withPassword(password);
        }
        if(dbName!=null) {
            builder.withDatabase(Integer.valueOf(dbName));
        }
        if(timeout!=null) {
            builder.withTimeout(timeout, TimeUnit.MILLISECONDS );
        }

        if(weight!=null) {
            ZStoreArgs.Builder.weights(weight);
        }

        return builder.build();
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


    @Override
    public void setDbName(String dbName) {
        try {
            Integer.valueOf(dbName);
            super.setDbName(dbName);
        } catch (NumberFormatException e) {
            super.setDbName(null);
        }
    }
}