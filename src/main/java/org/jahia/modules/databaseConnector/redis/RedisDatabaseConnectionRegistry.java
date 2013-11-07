package org.jahia.modules.databaseConnector.redis;

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
public class RedisDatabaseConnectionRegistry extends AbstractDatabaseConnectionRegistry<RedisDatabaseConnection> {

    private static final Logger logger = LoggerFactory.getLogger(RedisDatabaseConnectionRegistry.class);

    public RedisDatabaseConnectionRegistry() {
        super();
    }

    @Override
    public Map<String, RedisDatabaseConnection> populateRegistry() {
        JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {

            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                QueryResult queryResult = query("SELECT * FROM [dc:redisConnection]", session);
                NodeIterator it = queryResult.getNodes();
                while (it.hasNext()) {
                    JCRNodeWrapper connection = (JCRNodeWrapper) it.next();
                    String id = connection.getProperty("dc:id").getString();
                    String host = connection.getProperty("dc:host").getString();
                    Integer port = (int) connection.getProperty("dc:port").getLong();
                    RedisDatabaseConnection storedConnection = new RedisDatabaseConnection(id, host, port);
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
        // TODO
    }
}
