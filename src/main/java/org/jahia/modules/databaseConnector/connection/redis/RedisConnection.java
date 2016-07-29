package org.jahia.modules.databaseConnector.connection.redis;

import com.lambdaworks.redis.*;
import com.lambdaworks.redis.cluster.ClusterClientOptions;
import com.lambdaworks.redis.cluster.RedisClusterClient;
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

    private RedisClusterClient redisClusterClient;

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
        redisConnectionData.setOptions(options);
        return redisConnectionData;
    }

    @Override
    public Object getServerStatus() {
        if (redisClusterClient != null) {
            return redisClusterClient.connectCluster().info();
        }

        if (redisClient != null) {
            return redisClient.connect().info();
        }
        return null;
    }

    @Override
    protected Object beforeRegisterAsService() {
        if (!StringUtils.isEmpty(options)) {
            try {
                JSONObject jsonOptions = new JSONObject(options);
                if (jsonOptions.has("cluster")) {
                    redisClusterClient = RedisClusterClient.create(buildRedisClientUri(true));
                        if (jsonOptions.getJSONObject("cluster").has("refreshClusterView") && Boolean.valueOf((jsonOptions.getJSONObject("cluster")).getBoolean("refreshClusterView"))) {
                            redisClusterClient.setOptions(new ClusterClientOptions.Builder()
                                    .refreshClusterView(true)
                                    .refreshPeriod(jsonOptions.getJSONObject("cluster").getInt("refreshPeriod"), TimeUnit.SECONDS).build()
                            );
                        }
                        return redisClusterClient.connectCluster();

                }
            } catch(JSONException ex) {
                logger.error("Invalid JSON object", ex.getMessage());
            }
        }

        redisClient = RedisClient.create(buildRedisClientUri(false));

        return redisClient.connect();
    }

    @Override
    public void beforeUnregisterAsService() {
        if (redisClient != null) {
            redisClient.shutdown();
        }
        if (redisClusterClient != null) {
            redisClusterClient.shutdown();
        }

    }

    @Override
    public boolean testConnectionCreation() {
        try {
            if (!StringUtils.isEmpty(options)) {
                JSONObject jsonOptions = new JSONObject(options);
                if (jsonOptions.has("cluster")) {
                    RedisClusterClient redisClusterClient = RedisClusterClient.create(buildRedisClientUri(true));
                    try {
                        if (jsonOptions.getJSONObject("cluster").has("refreshClusterView") && Boolean.valueOf((jsonOptions.getJSONObject("cluster")).getBoolean("refreshClusterView"))) {
                            redisClusterClient.setOptions(new ClusterClientOptions.Builder()
                                    .refreshClusterView(true)
                                    .refreshPeriod(jsonOptions.getJSONObject("cluster").getInt("refreshPeriod"), TimeUnit.SECONDS).build()
                            );
                        }
                        redisClusterClient.connectCluster();
                        return true;
                    } catch(JSONException ex) {
                        logger.error("Invalid JSON object", ex.getMessage());
                    }
                }
            }

            RedisClient redisClient = RedisClient.create(buildRedisClientUri(false));

            redisClient.connect();
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    @Override
    public String parseOptions(LinkedHashMap options) {
        JSONObject formattedOptions = new JSONObject();
        try {
            if (options.containsKey("clusterSettings")) {
                JSONObject jsonCluster = new JSONObject();
                if (((LinkedHashMap)options.get("clusterSettings")).containsKey("refreshPeriod") && Integer.parseInt((String)(((LinkedHashMap)options.get("clusterSettings")).get("refreshPeriod"))) > 0) {
                    jsonCluster.put("refreshClusterView", true);
                    jsonCluster.put("refreshPeriod", ((LinkedHashMap)options.get("clusterSettings")).get("refreshPeriod"));
                } else {
                    jsonCluster.put("refreshClusterView", false);
                }
                formattedOptions.put("cluster", jsonCluster);
            }
        } catch(JSONException ex) {
            logger.error("Failed to serialize imported connection options", ex.getMessage());
        }
        return formattedOptions.toString();
    }


    private RedisURI buildRedisClientUri(boolean isCluster) {
//        redis :// [password@] host [: port] [/ database] [? [timeout=timeout[d|h|m|s|ms|us|ns]] [&database=database]]

        RedisURI.Builder builder = RedisURI.Builder.redis(host, port);
        if (password != null) {
            builder.withPassword(password);
        }
        if(dbName!=null && !isCluster) {
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

        if (this.timeout != null) {
            serializedString.append(TABU + "timeout " + DOUBLE_QUOTE + this.timeout + DOUBLE_QUOTE + NEW_LINE);
        }

        if (this.weight != null) {
            serializedString.append(TABU + "weight " + DOUBLE_QUOTE + this.weight + DOUBLE_QUOTE + NEW_LINE);
        }

        if (this.options != null) {
            try {
                JSONObject jsonOptions = new JSONObject(this.options);
                serializedString.append(TABU + "options {");
                //Handle cluster settings
                if (jsonOptions.has("cluster")) {
                    JSONObject jsonCluster = jsonOptions.getJSONObject("cluster");
                    serializedString.append(NEW_LINE + TABU + TABU + "clusterSettings {");
                    if (jsonCluster.has("refreshClusterView") && !StringUtils.isEmpty(jsonCluster.getString("refreshClusterView"))) {
                        serializedString.append(NEW_LINE + TABU + TABU + TABU + "refreshPeriod " + DOUBLE_QUOTE + jsonCluster.getString("refreshPeriod") + DOUBLE_QUOTE);
                    } else {
                        serializedString.append(NEW_LINE + TABU + TABU + TABU + "refreshPeriod " + DOUBLE_QUOTE + 0 + DOUBLE_QUOTE);
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