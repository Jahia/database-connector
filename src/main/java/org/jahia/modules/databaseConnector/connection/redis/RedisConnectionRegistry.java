package org.jahia.modules.databaseConnector.connection.redis;

import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.connection.AbstractDatabaseConnectionRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import java.util.Map;

import static org.jahia.modules.databaseConnector.connection.AbstractConnection.*;
import static org.jahia.modules.databaseConnector.Utils.query;
import static org.jahia.modules.databaseConnector.connection.redis.RedisConnection.*;

/**
 * Date: 11/6/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class RedisConnectionRegistry extends AbstractDatabaseConnectionRegistry<RedisConnection> {

    private static final Logger logger = LoggerFactory.getLogger(RedisConnectionRegistry.class);

    public RedisConnectionRegistry() {
        super();
    }

    @Override
    public Map<String, RedisConnection> populateRegistry() {
        JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {

            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                QueryResult queryResult = query("SELECT * FROM ["+ NODE_TYPE +"]", session);
                NodeIterator it = queryResult.getNodes();
                while (it.hasNext()) {
                    JCRNodeWrapper connectionNode = (JCRNodeWrapper) it.next();
                    String id = setStringConnectionProperty(connectionNode, ID_KEY, true);
                    String host = setStringConnectionProperty(connectionNode, HOST_KEY, true);
                    Integer port = setIntegerConnectionProperty(connectionNode, PORT_KEY, true);
                    Boolean isConnected = setBooleanConnectionProperty(connectionNode, IS_CONNECTED_KEY);
                    String password = decodePassword(connectionNode, PASSWORD_KEY);
                    String user = setStringConnectionProperty(connectionNode, USER_KEY, false);
                    Integer timeout = setIntegerConnectionProperty(connectionNode, TIMEOUT_KEY, false);
                    Integer weight = setIntegerConnectionProperty(connectionNode, WEIGHT_KEY, false);
                    RedisConnection storedConnection =
                            new RedisConnection(id, host, port, isConnected, password, user, null, timeout, weight);
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
    public boolean addEditConnection(final AbstractConnection connection, final Boolean isEdition) {
        Assert.hasText(connection.getHost(), "Host must be defined");
        Assert.notNull(connection.getPort(), "Port must be defined");
        if (isEdition) {
            ((RedisConnection) registry.get(connection.getOldId())).unregisterAsService();
        }
        RedisConnection redisConnection = (RedisConnection) connection;
        if (storeConnection(connection, NODE_TYPE, isEdition)) {
            if (isEdition) {
                if (!connection.getId().equals(connection.getOldId())) {
                    registry.remove(connection.getOldId());
                }
                registry.put(connection.getId(), redisConnection);
            }
            else {
                registry.put(connection.getId(), redisConnection);
            }
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    protected void storeAdvancedConfig(AbstractConnection connection, JCRNodeWrapper node) throws RepositoryException {
        RedisConnection redisConnection = (RedisConnection) connection;
        if (redisConnection.getTimeout() != null) {
            node.setProperty(TIMEOUT_KEY, redisConnection.getTimeout());
        }
        if (redisConnection.getWeight() != null) {
            node.setProperty(WEIGHT_KEY, redisConnection.getWeight());
        }
    }
}
