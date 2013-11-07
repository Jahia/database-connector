package org.jahia.modules.databaseConnector.neo4j;

import org.jahia.modules.databaseConnector.AbstractDatabaseConnectionRegistry;
import org.jahia.modules.databaseConnector.webflow.model.Connection;
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

import static org.jahia.modules.databaseConnector.neo4j.Neo4jDatabaseConnection.*;

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
                QueryResult queryResult = query("SELECT * FROM ["+ NODE_TYPE +"]", session);
                NodeIterator it = queryResult.getNodes();
                while (it.hasNext()) {
                    JCRNodeWrapper connection = (JCRNodeWrapper) it.next();
                    String id = connection.getProperty(ID_KEY).getString();
                    String uri = connection.getProperty(URI_KEY).getString();
                    String user = connection.hasProperty(USER_KEY) ? connection.getProperty(USER_KEY).getString() : null;
                    String password = connection.hasProperty(PASSWORD_KEY) ? connection.getProperty(PASSWORD_KEY).getString() : null;
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
        Assert.hasText(connection.getUri(), "URI must be defined");
        Neo4jDatabaseConnection neo4jDatabaseConnection =
                new Neo4jDatabaseConnection(connection.getId(), connection.getUri(), connection.getUser(), connection.getPassword());
        if (storeConnection(connection, NODE_TYPE)) {
            registry.put(connection.getId(), neo4jDatabaseConnection);
        }
        else {
            // TODO
        }
    }
}
