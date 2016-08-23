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

import static org.jahia.modules.databaseConnector.Utils.query;
import static org.jahia.modules.databaseConnector.connection.AbstractConnection.DB_NAME_KEY;
import static org.jahia.modules.databaseConnector.connection.AbstractConnection.HOST_KEY;
import static org.jahia.modules.databaseConnector.connection.AbstractConnection.ID_KEY;
import static org.jahia.modules.databaseConnector.connection.AbstractConnection.IS_CONNECTED_KEY;
import static org.jahia.modules.databaseConnector.connection.AbstractConnection.PASSWORD_KEY;
import static org.jahia.modules.databaseConnector.connection.AbstractConnection.PORT_KEY;
import static org.jahia.modules.databaseConnector.connection.redis.RedisConnection.*;
import static org.jahia.modules.databaseConnector.connection.redis.RedisConnection.OPTIONS_KEY;


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
                QueryResult queryResult = query("SELECT * FROM [" + NODE_TYPE + "]", session);
                NodeIterator it = queryResult.getNodes();
                while (it.hasNext()) {
                    JCRNodeWrapper connectionNode = (JCRNodeWrapper) it.next();
                    String id = setStringConnectionProperty(connectionNode, ID_KEY, true);
                    String host = setStringConnectionProperty(connectionNode, HOST_KEY, true);
                    Integer port = setIntegerConnectionProperty(connectionNode, PORT_KEY, true);
                    Boolean isConnected = setBooleanConnectionProperty(connectionNode, IS_CONNECTED_KEY);
                    String dbName = setStringConnectionProperty(connectionNode, DB_NAME_KEY, false);
                    String password = decodePassword(connectionNode, PASSWORD_KEY);
                    Long timeout = setLongConnectionProperty(connectionNode, TIMEOUT_KEY, false);
                    Integer weight = setIntegerConnectionProperty(connectionNode, WEIGHT_KEY, false);
                    String options = setStringConnectionProperty(connectionNode, OPTIONS_KEY, false);
                    RedisConnection storedConnection = new RedisConnection(id);
                    storedConnection.setOldId(id);
                    storedConnection.setHost(host);
                    storedConnection.setPort(port);
                    storedConnection.isConnected(isConnected);
                    storedConnection.setDbName(dbName);
                    storedConnection.setPassword(password);
                    storedConnection.setTimeout(timeout);
                    storedConnection.setWeight(weight);
                    storedConnection.setOptions(options);
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

        RedisConnection redisConnection = (RedisConnection) connection;
        if (storeConnection(redisConnection, NODE_TYPE, isEdition)) {
            if (isEdition) {
                if (registry.get(redisConnection.getOldId()).isConnected()) {
                    registry.get(connection.getOldId()).unregisterAsService();
                }
                if (!redisConnection.getId().equals(connection.getOldId())) {
                    registry.remove(connection.getOldId());
                }
                if (redisConnection.isConnected() && redisConnection.testConnectionCreation()) {
                    redisConnection.registerAsService();
                } else {
                    redisConnection.isConnected(false);
                }
                registry.put(redisConnection.getId(), redisConnection);

            } else {

                registry.put(redisConnection.getId(), redisConnection);
                if (redisConnection.isConnected() && redisConnection.testConnectionCreation()) {
                    redisConnection.registerAsService();
                } else {
                    redisConnection.isConnected(false);
                }

            }
            return true;

        } else {
            return false;
        }
    }


    @Override
    public boolean importConnection(Map<String, Object> map) {
        return false;
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

