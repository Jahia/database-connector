package org.jahia.modules.databaseConnector.webflow.model.redis;

import org.jahia.modules.databaseConnector.ConnectionData;
import org.jahia.modules.databaseConnector.DatabaseTypes;
import org.jahia.modules.databaseConnector.redis.RedisConnectionDataImpl;
import org.jahia.modules.databaseConnector.webflow.model.AbstractConnection;
import redis.clients.jedis.Protocol;
import redis.clients.util.Sharded;

/**
 * Date: 11/18/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class RedisConnectionImpl extends AbstractConnection implements RedisConnection {

    private Integer timeout = Protocol.DEFAULT_TIMEOUT;

    private Integer weight = Sharded.DEFAULT_WEIGHT;

    public RedisConnectionImpl() {
        super(DatabaseTypes.REDIS);
        this.host = "localhost";
        this.port = Protocol.DEFAULT_PORT;
    }

    public RedisConnectionImpl(ConnectionData connectionData) {
        super(connectionData);
        RedisConnectionDataImpl redisConnectionData = (RedisConnectionDataImpl) connectionData;
        this.timeout = redisConnectionData.getTimeout();
        this.weight = redisConnectionData.getWeight();
    }

    @Override
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @Override
    public Integer getTimeout() {
        return timeout;
    }

    @Override
    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public Integer getWeight() {
        return weight;
    }

    @Override
    public void validateEnterConfig() {
        if (host == null || host.isEmpty()) {
            addRequiredErrorMessage("host");
        }
        if (port == null) {
            addRequiredErrorMessage("port");
        } else if (port <= 0) {
            addErrorMessage("port", "dc_databaseConnector.label.port.message.askPositiveInteger");
        }
    }
}
