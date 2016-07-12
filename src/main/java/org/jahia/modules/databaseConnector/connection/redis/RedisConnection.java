package org.jahia.modules.databaseConnector.connection.redis;

import org.apache.commons.httpclient.URI;
import org.apache.commons.lang.StringUtils;
import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.connection.DatabaseTypes;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;
import redis.clients.util.Sharded;

import java.util.LinkedHashMap;

/**
 * @author by stefan on 2016-05-11.
 */
public class RedisConnection extends AbstractConnection {

    public static final String NODE_TYPE = "dc:redisConnection";

    private Integer timeout = Protocol.DEFAULT_TIMEOUT;

    private Integer weight = Sharded.DEFAULT_WEIGHT;

    public static final String TIMEOUT_KEY = "dc:timeout";

    public static final String WEIGHT_KEY = "dc:weight";

    private Jedis redisClient;


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

    @Override
    public String getSerializedExportData() {
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
}