package org.jahia.modules.databaseConnector.connection.mongo;

import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.connection.AbstractDatabaseConnectionRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
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
                    String id = setStringConnectionProperty(connectionNode, ID_KEY, true);
                    String host = setStringConnectionProperty(connectionNode, HOST_KEY, true);
                    Integer port = setIntegerConnectionProperty(connectionNode, PORT_KEY, true);
                    Boolean isConnected = setBooleanConnectionProperty(connectionNode, IS_CONNECTED_KEY);
                    String dbName = setStringConnectionProperty(connectionNode, DB_NAME_KEY, false);
                    String user = setStringConnectionProperty(connectionNode, USER_KEY, false);
                    String password = decodePassword(connectionNode, PASSWORD_KEY);
                    String writeConcern = setStringConnectionProperty(connectionNode, WRITE_CONCERN_KEY, false);
                    String authDb = setStringConnectionProperty(connectionNode, AUTH_DB_KEY, false);
                    MongoConnection storedConnection = new MongoConnection(id);
                    storedConnection.setHost(host);
                    storedConnection.setPort(port);
                    storedConnection.isConnected(isConnected);
                    storedConnection.setDbName(dbName);
                    storedConnection.setUser(user);
                    storedConnection.setPassword(password);
                    storedConnection.setWriteConcern(writeConcern);
                    storedConnection.setAuthDb(authDb);
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

    public boolean addEditConnection(final AbstractConnection connection, final Boolean isEdition) {
        MongoConnection mongoConnection = (MongoConnection) connection;
        if (storeConnection(mongoConnection, NODE_TYPE, isEdition)) {
            if (isEdition) {
                if (!mongoConnection.getId().equals(mongoConnection.getOldId())) {
                    //If this connection has a new id, un register previous connection if it was registered.
                    if (registry.get(mongoConnection.getOldId()).isConnected()) {
                        registry.get(mongoConnection.getOldId()).unregisterAsService();
                    }
                    //If new connection is connected, register it.
                    if (mongoConnection.isConnected()) {
                        mongoConnection.registerAsService();
                    }
                    //remove the old unused connection.
                    registry.remove(mongoConnection.getOldId());
                } else {
                    //If this is the same connection, register it if it was previously un registered, or vice versa.
                    if(!mongoConnection.isConnected() && registry.get(mongoConnection.getId()).isConnected()) {
                        registry.get(mongoConnection.getId()).unregisterAsService();
                    } else if(mongoConnection.isConnected() && !registry.get(mongoConnection.getId()).isConnected()) {
                        mongoConnection.registerAsService();
                    }
                }
                //Add the new modified connection to the registry.
                registry.put(mongoConnection.getId(), mongoConnection);
            } else {
                //If this is a new connection, just add it to registry and register the service if it should be connected.
                registry.put(mongoConnection.getId(), mongoConnection);
                if (mongoConnection.isConnected()) {
                    mongoConnection.registerAsService();
                }
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
