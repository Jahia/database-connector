package org.jahia.modules.databaseConnector;

import org.jahia.modules.databaseConnector.neo4j.Neo4jDatabaseConnection;
import org.jahia.modules.databaseConnector.webflow.model.Connection;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.Map;
import java.util.TreeMap;

import static org.jahia.modules.databaseConnector.AbstractDatabaseConnection.*;
import static org.jahia.modules.databaseConnector.DatabaseConnectorManager.*;

/**
 * Date: 11/6/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public abstract class AbstractDatabaseConnectionRegistry<T extends AbstractDatabaseConnection> implements DatabaseConnectionRegistry<T> {

    private final static String NODE_TYPE = "dcmix:databaseConnection";

    private static final Logger logger = LoggerFactory.getLogger(AbstractDatabaseConnectionRegistry.class);

    protected Map<String, T> registry;

    protected JCRTemplate jcrTemplate;

    public AbstractDatabaseConnectionRegistry() {
        this.jcrTemplate = JCRTemplate.getInstance();
        this.registry = new TreeMap<String, T>();
    }

    @Override
    public Map<String, T> getRegistry() {
        return registry;
    }

    protected Boolean storeConnection(final Connection connection, final String nodeType) {
        JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {

            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper databaseConnectorNode = getDatabaseConnectorNode(session);
                session.checkout(databaseConnectorNode);
                JCRNodeWrapper connectionNode = databaseConnectorNode.addNode(connection.getId(), nodeType);
                connectionNode.setProperty(ID_KEY, connection.getId());
                if (connection.getHost() != null && !connection.getHost().isEmpty()) {
                    connectionNode.setProperty(HOST_KEY, connection.getHost());
                }
                if (connection.getPort() != null) {
                    connectionNode.setProperty(PORT_KEY, connection.getPort());
                }
                if (connection.getUri() != null && !connection.getUri().isEmpty()) {
                    connectionNode.setProperty(URI_KEY, connection.getUri());
                }
                if (connection.getDbName() != null && !connection.getDbName().isEmpty()) {
                    connectionNode.setProperty(DB_NAME_KEY, connection.getDbName());
                }
                if (connection.getUser() != null && !connection.getUser().isEmpty()) {
                    connectionNode.setProperty(USER_KEY, connection.getUser());
                }
                if (connection.getPassword() != null && !connection.getPassword().isEmpty()) {
                    connectionNode.setProperty(PASSWORD_KEY, connection.getPassword());
                }
                session.save();
                return true;
            }
        };
        try {
            return jcrTemplate.doExecuteWithSystemSession(callback);
        } catch (RepositoryException e) {
            return false;
        }
    }

    @Override
    public Boolean removeConnection(final String databaseConnectionId) {
        if (!registry.containsKey(databaseConnectionId)) {
            throw new IllegalArgumentException("No database connection with ID: " + databaseConnectionId);
        }
        JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {

            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                StringBuffer statement = new StringBuffer("SELECT * FROM [").append(NODE_TYPE).append("] WHERE [").
                        append(ID_KEY).append("] = '").append(databaseConnectionId).append("'");
                NodeIterator nodes = query(statement.toString(), session).getNodes();
                if (!nodes.hasNext()) {
                    // TODO
                    return false;
                }
                Node databaseConnectionNode = nodes.nextNode();
                session.checkout(databaseConnectionNode);
                session.removeItem(databaseConnectionNode.getPath());
                session.save();
                return true;
            }
        };
        try {
            if (jcrTemplate.doExecuteWithSystemSession(callback)) {
                registry.remove(databaseConnectionId);
                return true;
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    protected QueryResult query(String statement, JCRSessionWrapper session) throws RepositoryException {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query query = queryManager.createQuery(statement, Query.JCR_SQL2);
        return query.execute();
    }

    protected JCRNodeWrapper getDatabaseConnectorNode(JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper settings = session.getNode(DATABASE_CONNECTOR_ROOT_PATH);
        if (settings.hasNode(DATABASE_CONNECTOR_PATH)) {
            return settings.getNode(DATABASE_CONNECTOR_PATH);
        }
        else {
            return settings.addNode(DATABASE_CONNECTOR_PATH, DATABASE_CONNECTOR_NODE_TYPE);
        }
    }
}
