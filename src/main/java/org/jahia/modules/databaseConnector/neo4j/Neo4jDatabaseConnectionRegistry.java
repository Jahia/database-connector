package org.jahia.modules.databaseConnector.neo4j;

import org.jahia.modules.databaseConnector.AbstractDatabaseConnectionRegistry;
import org.jahia.modules.databaseConnector.webflow.model.Connection;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import java.util.Map;

/**
 * Date: 11/6/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class Neo4jDatabaseConnectionRegistry extends AbstractDatabaseConnectionRegistry<Neo4jDatabaseConnection> {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jDatabaseConnectionRegistry.class);

    public Neo4jDatabaseConnectionRegistry() {
        super();
    }

    @Override
    public Map<String, Neo4jDatabaseConnection> populateRegistry() {
        JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {

            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                QueryResult queryResult = query("SELECT * FROM [dc:neo4jConnection]", session);
                NodeIterator it = queryResult.getNodes();
                while (it.hasNext()) {
                    JCRNodeWrapper connection = (JCRNodeWrapper) it.next();
                    String id = connection.getProperty("dc:id").getString();
                    String uri = connection.getProperty("dc:uri").getString();
                    String user = connection.hasProperty("dc:user") ? connection.getProperty("dc:user").getString() : null;
                    String password = connection.hasProperty("dc:password") ? connection.getProperty("dc:password").getString() : null;
                    Neo4jDatabaseConnection storedConnection = new Neo4jDatabaseConnection(id, uri, user, password);
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
    public void addConnection(Connection connection) {
        Neo4jDatabaseConnection neo4jDatabaseConnection =
                new Neo4jDatabaseConnection(connection.getId(), connection.getUri(), connection.getUser(), connection.getPassword());
        if (storeConnection(connection)) {
            registry.put(connection.getId(), neo4jDatabaseConnection);
        }
        else {
            // TODO
        }
    }

    private Boolean storeConnection(final Connection connection) {
        JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {

            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper databaseConnectorNode = getDatabaseConnectorNode(session);
                session.checkout(databaseConnectorNode);
                JCRNodeWrapper connectionNode = databaseConnectorNode.addNode(connection.getId(), "dc:neo4jConnection");
                connectionNode.setProperty("dc:id", connection.getId());
                connectionNode.setProperty("dc:uri", connection.getUri());
                if (connection.getUser() != null && !connection.getUser().isEmpty()) {
                    connectionNode.setProperty("dc:user", connection.getUser());
                }
                if (connection.getPassword() != null && !connection.getPassword().isEmpty()) {
                    connectionNode.setProperty("dc:password", connection.getPassword());
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
}
