package org.jahia.modules.databaseConnector.connection.redis;

import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.connection.DatabaseTypes;
import redis.clients.jedis.Protocol;
import redis.clients.util.Sharded;

/**
 * @author by stefan on 2016-05-11.
 */
public class RedisConnection extends AbstractConnection {

    public static final String NODE_TYPE = "dc:redisConnection";

    private Integer timeout = Protocol.DEFAULT_TIMEOUT;

    private Integer weight = Sharded.DEFAULT_WEIGHT;

    public static final String TIMEOUT_KEY = "dc:timeout";

    public static final String WEIGHT_KEY = "dc:weight";

    private static final DatabaseTypes DATABASE_TYPE = DatabaseTypes.REDIS;

    public RedisConnection(String id, String host, Integer port, Boolean isConnected, String dbName,
                           String user, String password, Integer timeout, Integer weight) {
        this(id, host, port, isConnected, dbName, null, user, password, timeout, weight);
    }

    public RedisConnection(String id, String host, Integer port, Boolean isConnected, String dbName, String uri,
                           String user, String password, Integer timeout, Integer weight) {
        super(id, host, port, isConnected, dbName, user, password, uri, DATABASE_TYPE);
        this.timeout = timeout;
        this.weight = weight;
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

    @Override
    public RedisConnectionData makeConnectionData() {
        return new RedisConnectionData(id, host, port, isConnected, dbName, uri, user, password, databaseType, timeout, weight);
    }

    @Override
    protected Object beforeRegisterAsService() {
        return null;
    }

    @Override
    public void beforeUnregisterAsService() {

    }
}
