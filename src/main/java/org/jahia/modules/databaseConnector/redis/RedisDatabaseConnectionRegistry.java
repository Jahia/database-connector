package org.jahia.modules.databaseConnector.redis;

import org.jahia.modules.databaseConnector.AbstractDatabaseConnectionRegistry;
import org.jahia.modules.databaseConnector.webflow.model.Connection;
import org.jahia.modules.databaseConnector.webflow.model.redis.RedisConnectionImpl;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import redis.clients.jedis.JedisShardInfo;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import java.util.Map;

import static org.jahia.modules.databaseConnector.AbstractDatabaseConnection.HOST_KEY;
import static org.jahia.modules.databaseConnector.AbstractDatabaseConnection.ID_KEY;
import static org.jahia.modules.databaseConnector.AbstractDatabaseConnection.PASSWORD_KEY;
import static org.jahia.modules.databaseConnector.AbstractDatabaseConnection.PORT_KEY;
import static org.jahia.modules.databaseConnector.redis.RedisDatabaseConnectionImpl.*;

/**
 * Date: 11/6/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class RedisDatabaseConnectionRegistry extends AbstractDatabaseConnectionRegistry<RedisDatabaseConnection> {

    private static final Logger logger = LoggerFactory.getLogger(RedisDatabaseConnectionRegistry.class);

    public RedisDatabaseConnectionRegistry() {
        super();
    }

    @Override
    public Map<String, RedisDatabaseConnection> populateRegistry() {
        JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {

            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                QueryResult queryResult = query("SELECT * FROM ["+ NODE_TYPE +"]", session);
                NodeIterator it = queryResult.getNodes();
                while (it.hasNext()) {
                    JCRNodeWrapper connection = (JCRNodeWrapper) it.next();
                    String id = connection.getProperty(ID_KEY).getString();
                    String host = connection.getProperty(HOST_KEY).getString();
                    Integer port = (int) connection.getProperty(PORT_KEY).getLong();
                    String password = connection.hasProperty(PASSWORD_KEY) ?
                            connection.getProperty(PASSWORD_KEY).getString() : null;
                    Integer timeout = connection.hasProperty(TIMEOUT_KEY) ?
                            (int) connection.getProperty(TIMEOUT_KEY).getLong() : null;
                    Integer weight = connection.hasProperty(WEIGHT_KEY) ?
                            (int) connection.getProperty(WEIGHT_KEY).getLong() : null;
                    JedisShardInfo shardInfo;
                    if (timeout == null) {
                        shardInfo = new JedisShardInfo(host, port);
                    }
                    else {
                        if (weight != null) {
                            shardInfo = new JedisShardInfo(host, port, timeout, weight);
                        }
                        else {
                            shardInfo = new JedisShardInfo(host, port);
                            shardInfo.setTimeout(timeout);
                        }
                    }
                    shardInfo.setPassword(password);
                    RedisDatabaseConnectionImpl storedConnection = new RedisDatabaseConnectionImpl(id, shardInfo);
                    registry.put(id, storedConnection);
                }
                return true;
            }
        };
        try {
            jcrTemplate.doExecuteWithSystemSession(callback);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return registry;
    }

    @Override
    public void addEditConnection(Connection connection, boolean isEdition) {
        Assert.hasText(connection.getHost(), "Host must be defined");
        Assert.notNull(connection.getPort(), "Port must be defined");
        final RedisConnectionImpl redisConnection = (RedisConnectionImpl) connection;
        RedisDatabaseConnection redisDatabaseConnection = null;
        if (redisConnection.getTimeout() != null) {
            JedisShardInfo shardInfo = null;
            if (redisConnection.getWeight() != null) {
                shardInfo = new JedisShardInfo(redisConnection.getHost(), redisConnection.getPort(),
                        redisConnection.getTimeout(), redisConnection.getWeight());
            }
            else {
                shardInfo = new JedisShardInfo(redisConnection.getHost(), redisConnection.getPort(),
                        redisConnection.getTimeout());
            }
            redisDatabaseConnection = new RedisDatabaseConnectionImpl(redisConnection.getId(), shardInfo);
        }
        else {
            redisDatabaseConnection =
                    new RedisDatabaseConnectionImpl(connection.getId(), connection.getHost(), connection.getPort());
        }
        if (storeConnection(connection, NODE_TYPE, isEdition)) {
            if (isEdition) {
                if (!connection.getId().equals(connection.getOldId())) {
                    registry.remove(connection.getOldId());
                }
                registry.put(connection.getId(), redisDatabaseConnection);
            }
            else {
                registry.put(connection.getId(), redisDatabaseConnection);
            }
        }
        else {
            // TODO
        }
    }

    @Override
    protected void storeAdvancedConfig(Connection connection, JCRNodeWrapper node) throws RepositoryException {
        RedisConnectionImpl redisConnection = (RedisConnectionImpl) connection;
        if (redisConnection.getTimeout() != null) {
            node.setProperty(TIMEOUT_KEY, redisConnection.getTimeout());
        }
        if (redisConnection.getWeight() != null) {
            node.setProperty(WEIGHT_KEY, redisConnection.getWeight());
        }
    }
}
