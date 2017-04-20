package org.jahia.modules.databaseConnector.connection.redis;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.ZStoreArgs;
import com.lambdaworks.redis.cluster.ClusterClientOptions;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import com.lambdaworks.redis.resource.ClientResources;
import com.lambdaworks.redis.resource.DefaultClientResources;
import org.apache.commons.lang.StringUtils;
import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.utils.EncryptionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.jahia.modules.databaseConnector.util.Utils.*;

/**
 * @author by stefan on 2016-05-11.
 */
public class RedisConnection extends AbstractConnection {

    public static final String NODE_TYPE = "dc:redisConnection";
    public static final String TIMEOUT_KEY = "dc:timeout";
    public static final String WEIGHT_KEY = "dc:weight";
    public static final Integer DEFAULT_PORT = 6379;
    public static final String DEFAULT_DATABASE_NUMBER = "0";

    private static final Logger logger = LoggerFactory.getLogger(RedisConnection.class);
    public static final String DATABASE_TYPE = "Redis";
    public static final String DISPLAY_NAME = "RedisDB";
    private static final int TEST_CONNECTION_TIMEOUT = 5000;
    private RedisClient redisClient;
    private RedisClusterClient redisClusterClient;
    private Long timeout;
    private Integer weight;
    private static ClientResources clientResources = new DefaultClientResources.Builder().ioThreadPoolSize(16).computationThreadPoolSize(16).build();

    public RedisConnection(String id) {
        this.id = id;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public RedisConnectionData makeConnectionData() {
        RedisConnectionData redisConnectionData = new RedisConnectionData(id);
        redisConnectionData.setHost(host);
        redisConnectionData.setPort(port == null ? DEFAULT_PORT : port);
        redisConnectionData.isConnected(isConnected);
        redisConnectionData.setDbName(dbName == null ? DEFAULT_DATABASE_NUMBER : dbName);
        redisConnectionData.setPassword(password);
        redisConnectionData.setTimeout(timeout);
        redisConnectionData.setWeight(weight);
        redisConnectionData.setDatabaseType(DATABASE_TYPE);
        redisConnectionData.setOptions(options);
        redisConnectionData.setDisplayName(DISPLAY_NAME);
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
                    redisClusterClient = RedisClusterClient.create(clientResources, buildRedisClientUri(true, false));
                    if (jsonOptions.getJSONObject("cluster").has("refreshClusterView") && (jsonOptions.getJSONObject("cluster")).getBoolean("refreshClusterView")) {
                        redisClusterClient.setOptions(new ClusterClientOptions.Builder()
                                .refreshClusterView(true)
                                .refreshPeriod(jsonOptions.getJSONObject("cluster").getInt("refreshPeriod"), TimeUnit.SECONDS).build()
                        );
                    }
                    return redisClusterClient.connectCluster();

                }
            } catch (JSONException ex) {
                logger.error("Invalid JSON object", ex.getMessage());
            }
        }

        redisClient = RedisClient.create(clientResources, buildRedisClientUri(false, false));

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
        RedisClient redisClientTest = null;
        RedisClusterClient redisClusterClientTest = null;
        try {
            if (!StringUtils.isEmpty(options)) {
                JSONObject jsonOptions = new JSONObject(options);
                if (jsonOptions.has("cluster")) {
                    redisClusterClientTest = RedisClusterClient.create(clientResources, buildRedisClientUri(true, true));
                    try {
                        if (jsonOptions.getJSONObject("cluster").has("refreshClusterView") && (jsonOptions.getJSONObject("cluster")).getBoolean("refreshClusterView")) {
                            redisClusterClientTest.setOptions(new ClusterClientOptions.Builder()
                                    .refreshClusterView(true)
                                    .refreshPeriod(jsonOptions.getJSONObject("cluster").getInt("refreshPeriod"), TimeUnit.SECONDS).build()
                            );
                        }
                        redisClusterClientTest.connectCluster();
                        return true;
                    } catch (JSONException ex) {
                        logger.error("Invalid JSON object", ex.getMessage());
                    }
                }
            }


            redisClientTest = RedisClient.create(clientResources, buildRedisClientUri(false, true));

            redisClientTest.connect();
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        } finally {
            if(redisClientTest != null) {
                redisClientTest.shutdown();
            }
            if(redisClusterClientTest != null) {
                redisClusterClientTest.shutdown();
            }
        }
    }

    @Override
    public String parseOptions(LinkedHashMap options) {
        JSONObject formattedOptions = new JSONObject();
        try {
            if (options.containsKey("clusterSettings")) {
                JSONObject jsonCluster = new JSONObject();
                if (((LinkedHashMap) options.get("clusterSettings")).containsKey("refreshPeriod") && Integer.parseInt((String) (((LinkedHashMap) options.get("clusterSettings")).get("refreshPeriod"))) > 0) {
                    jsonCluster.put("refreshClusterView", true);
                    jsonCluster.put("refreshPeriod", ((LinkedHashMap) options.get("clusterSettings")).get("refreshPeriod"));
                } else {
                    jsonCluster.put("refreshClusterView", false);
                }
                formattedOptions.put("cluster", jsonCluster);
            }
        } catch (JSONException ex) {
            logger.error("Failed to serialize imported connection options", ex.getMessage());
        }
        return formattedOptions.toString();
    }


    private RedisURI buildRedisClientUri(boolean isCluster, boolean isTest) {
//        redis :// [password@] host [: port] [/ database] [? [timeout=timeout[d|h|m|s|ms|us|ns]] [&database=database]]
        RedisURI.Builder builder = RedisURI.Builder.redis(host, port != null ? port : DEFAULT_PORT);
        if (password != null) {
            builder.withPassword(password);
        }
        //If Default database number is ever changed to anything but 0, cluster database number must be set to 0
        builder.withDatabase(isCluster || dbName == null ? Integer.valueOf(DEFAULT_DATABASE_NUMBER) : Integer.valueOf(dbName));

        if (isTest) {
            builder.withTimeout(TEST_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        } else if (timeout != null) {
            builder.withTimeout(timeout, TimeUnit.MILLISECONDS);
        }

        if (weight != null) {
            ZStoreArgs.Builder.weights(weight);
        }
        return builder.build();
    }

    public String getDatabaseType() {
        return DATABASE_TYPE;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }


    @Override
    public String getSerializedExportData() {
        StringBuilder serializedString = new StringBuilder();
        serializedString.append(TABU).append("type ").append(DOUBLE_QUOTE).append(DATABASE_TYPE).append(DOUBLE_QUOTE).append(NEW_LINE);
        serializedString.append(TABU).append("host ").append(DOUBLE_QUOTE).append(this.host).append(DOUBLE_QUOTE).append(NEW_LINE);
        serializedString.append(TABU).append("dbName ").append(DOUBLE_QUOTE).append(this.dbName != null ? this.dbName : DEFAULT_DATABASE_NUMBER).append(DOUBLE_QUOTE).append(NEW_LINE);
        serializedString.append(TABU).append("identifier ").append(DOUBLE_QUOTE).append(this.id).append(DOUBLE_QUOTE).append(NEW_LINE);
        serializedString.append(TABU).append("isConnected ").append(DOUBLE_QUOTE).append(this.isConnected()).append(DOUBLE_QUOTE).append(NEW_LINE);
        serializedString.append(TABU + "port " + DOUBLE_QUOTE).append(this.port != null ? this.port : DEFAULT_PORT).append(DOUBLE_QUOTE).append(NEW_LINE);

        if (!StringUtils.isEmpty(this.password)) {
            serializedString.append(TABU + "password " + DOUBLE_QUOTE).append(EncryptionUtils.passwordBaseEncrypt(this.password)).append("_ENC").append(DOUBLE_QUOTE).append(NEW_LINE);
        }

        if (this.timeout != null) {
            serializedString.append(TABU + "timeout " + DOUBLE_QUOTE).append(this.timeout).append(DOUBLE_QUOTE).append(NEW_LINE);
        }

        if (this.weight != null) {
            serializedString.append(TABU + "weight " + DOUBLE_QUOTE).append(this.weight).append(DOUBLE_QUOTE).append(NEW_LINE);
        }

        if (this.options != null) {
            try {
                JSONObject jsonOptions = new JSONObject(this.options);
                serializedString.append(TABU + "options {");
                //Handle cluster settings
                if (jsonOptions.has("cluster")) {
                    JSONObject jsonCluster = jsonOptions.getJSONObject("cluster");
                    serializedString.append(NEW_LINE).append(TABU).append(TABU).append("clusterSettings {");
                    if (jsonCluster.has("refreshClusterView") && !StringUtils.isEmpty(jsonCluster.getString("refreshClusterView"))) {
                        serializedString.append(NEW_LINE).append(TABU).append(TABU).append(TABU).append("refreshPeriod ").append(DOUBLE_QUOTE).append(jsonCluster.getString("refreshPeriod")).append(DOUBLE_QUOTE);
                    } else {
                        serializedString.append(NEW_LINE).append(TABU).append(TABU).append(TABU).append("refreshPeriod ").append(DOUBLE_QUOTE).append(0).append(DOUBLE_QUOTE);
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