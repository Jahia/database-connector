package org.jahia.modules.databaseConnector.mongo;

import org.jahia.modules.databaseConnector.AbstractDatabaseConnectionRegistry;
import org.jahia.modules.databaseConnector.webflow.model.Connection;
import org.jahia.modules.databaseConnector.webflow.model.mongo.MongoConnection;
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

import static org.jahia.modules.databaseConnector.AbstractDatabaseConnection.*;
import static org.jahia.modules.databaseConnector.mongo.MongoDatabaseConnection.WRITE_CONCERN_KEY;
import static org.jahia.modules.databaseConnector.mongo.MongoDatabaseConnectionImpl.NODE_TYPE;

/**
 * Date: 11/6/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class MongoDatabaseConnectionRegistry extends AbstractDatabaseConnectionRegistry<MongoDatabaseConnection> {

    private static final Logger logger = LoggerFactory.getLogger(MongoDatabaseConnectionRegistry.class);

    public MongoDatabaseConnectionRegistry() {
        super();
    }

    @Override
    public Map<String, MongoDatabaseConnection> populateRegistry() {
        JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {

            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                QueryResult queryResult = query("SELECT * FROM ["+ NODE_TYPE +"]", session);
                NodeIterator it = queryResult.getNodes();
                while (it.hasNext()) {
                    JCRNodeWrapper connection = (JCRNodeWrapper) it.next();
                    String id = connection.getProperty(ID_KEY).getString();
                    String host = connection.getProperty(HOST_KEY).getString();
                    Integer port = (int) connection.getProperty(PORT_KEY).getLong();
                    String dbName = connection.hasProperty(DB_NAME_KEY) ? connection.getProperty(DB_NAME_KEY).getString() : null;
                    String user = connection.hasProperty(USER_KEY) ? connection.getProperty(USER_KEY).getString() : null;
                    String password = connection.hasProperty(PASSWORD_KEY) ? decodePassword(connection.getProperty(PASSWORD_KEY).getString()) : null;
                    String writeConcern = connection.hasProperty(WRITE_CONCERN_KEY) ? connection.getProperty(WRITE_CONCERN_KEY).getString() : null;
                    try {
                        MongoDatabaseConnection storedConnection = new MongoDatabaseConnectionImpl(id, host, port, dbName, user, password, writeConcern);
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

    @Override
    public void addEditConnection(Connection connection, boolean isEdition) {
        Assert.hasText(connection.getHost(), "Host must be defined");
        Assert.notNull(connection.getPort(), "Port must be defined");
        Assert.hasText(connection.getDbName(), "DB name must be defined");
        MongoDatabaseConnection mongoDatabaseConnection =
                null;
        try {
            mongoDatabaseConnection = new MongoDatabaseConnectionImpl(connection.getId(), connection.getHost(), connection.getPort(),
                    connection.getDbName(), connection.getUser(), connection.getPassword(), ((MongoConnection) connection).getWriteConcern());
        } catch (UnknownHostException e) {
            logger.error(e.getMessage(), e);
        }
        if (storeConnection(connection, NODE_TYPE, isEdition)) {
            if (isEdition) {
                if (!connection.getId().equals(connection.getOldId())) {
                    registry.remove(connection.getOldId());
                }
                registry.put(connection.getId(), mongoDatabaseConnection);
            }
            else {
                registry.put(connection.getId(), mongoDatabaseConnection);
            }
        }
        else {
            // TODO
        }
    }

    @Override
    protected void storeAdvancedConfig(Connection connection, JCRNodeWrapper node) throws RepositoryException {
        MongoConnection mongoConnection = (MongoConnection) connection;
        node.setProperty(WRITE_CONCERN_KEY, mongoConnection.getWriteConcern());
    }
}
