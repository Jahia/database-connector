package org.jahia.modules.databaseConnector.connection.mongo;

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
import java.net.UnknownHostException;
import java.util.Map;

import static org.jahia.modules.databaseConnector.Utils.query;
import static org.jahia.modules.databaseConnector.connection.AbstractConnection.*;
import static org.jahia.modules.databaseConnector.connection.mongo.MongoConnection.AUTH_DB_KEY;
import static org.jahia.modules.databaseConnector.connection.mongo.MongoConnection.WRITE_CONCERN_KEY;
import static org.jahia.modules.databaseConnector.connection.mongo.MongoConnection.NODE_TYPE;

/**
 * Date: 11/6/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class MongoConnectionRegistry extends AbstractDatabaseConnectionRegistry<MongoConnection> {

    private static final Logger logger = LoggerFactory.getLogger(MongoConnectionRegistry.class);

    public MongoConnectionRegistry() {
        super();
    }

    @Override
    public Map<String, MongoConnection> populateRegistry() {
        JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {

            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                QueryResult queryResult = query("SELECT * FROM ["+ NODE_TYPE +"]", session);
                NodeIterator it = queryResult.getNodes();
                while (it.hasNext()) {
                    JCRNodeWrapper connectionNode = (JCRNodeWrapper) it.next();
                    String id = connectionNode.getProperty(ID_KEY).getString();
                    String host = connectionNode.getProperty(HOST_KEY).getString();
                    Integer port = (int) connectionNode.getProperty(PORT_KEY).getLong();
                    Boolean isConnected = connectionNode.getProperty(IS_CONNECTED_KEY).getBoolean();
                    String dbName = connectionNode.hasProperty(DB_NAME_KEY) ? connectionNode.getProperty(DB_NAME_KEY).getString() : null;
                    String user = connectionNode.hasProperty(USER_KEY) ? connectionNode.getProperty(USER_KEY).getString() : null;
                    String password = connectionNode.hasProperty(PASSWORD_KEY) ? decodePassword(connectionNode.getProperty(PASSWORD_KEY).getString()) : null;
                    String writeConcern = connectionNode.hasProperty(WRITE_CONCERN_KEY) ? connectionNode.getProperty(WRITE_CONCERN_KEY).getString() : null;
                    String dbAuth = connectionNode.hasProperty(AUTH_DB_KEY) ? connectionNode.getProperty(AUTH_DB_KEY).getString() : null;
                    try {
                        MongoConnection storedConnection = new MongoConnection(id, host, port, isConnected, dbName, user, password, dbAuth, writeConcern);
                        registry.put(id, storedConnection);
                    } catch (UnknownHostException e) {
                        logger.error(e.getMessage(), e);
                    }
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

    public boolean addEditConnection(final AbstractConnection connection, final Boolean isEdition) {
        Assert.hasText(connection.getHost(), "Host must be defined");
        Assert.notNull(connection.getPort(), "Port must be defined");
        Assert.hasText(connection.getDbName(), "DB name must be defined");
        MongoConnection mongoConnection = (MongoConnection) connection;
        if (storeConnection(connection, NODE_TYPE, isEdition)) {
            if (isEdition) {
                if (!connection.getId().equals(connection.getOldId())) {
                    if (registry.get(connection.getOldId()).isConnected()) {
                        registry.get(connection.getOldId()).unregisterAsService();
                    }
                    registry.remove(connection.getOldId());
                }
                registry.put(connection.getId(), mongoConnection);
            }
            else {
                registry.put(connection.getId(), mongoConnection);
            }
            if (connection.isConnected()) {
                ((MongoConnection) connection).beforeRegisterAsService();
            }
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    protected void storeAdvancedConfig(AbstractConnection connection, JCRNodeWrapper node) throws RepositoryException {
        MongoConnection mongoConnection = (MongoConnection) connection;
        node.setProperty(WRITE_CONCERN_KEY, mongoConnection.getWriteConcern());
        node.setProperty(AUTH_DB_KEY, mongoConnection.getAuthDb());
    }
}
