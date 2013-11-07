package org.jahia.modules.databaseConnector.mongo;

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
import java.net.UnknownHostException;
import java.util.Map;

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
                QueryResult queryResult = query("SELECT * FROM [dc:mongoConnection]", session);
                NodeIterator it = queryResult.getNodes();
                while (it.hasNext()) {
                    JCRNodeWrapper connection = (JCRNodeWrapper) it.next();
                    String id = connection.getProperty("dc:id").getString();
                    String host = connection.getProperty("dc:host").getString();
                    Integer port = (int) connection.getProperty("dc:port").getLong();
                    String dbName = connection.hasProperty("dc:dbName") ? connection.getProperty("dc:dbName").getString() : null;
                    String user = connection.hasProperty("dc:user") ? connection.getProperty("dc:user").getString() : null;
                    String password = connection.hasProperty("dc:password") ? connection.getProperty("dc:password").getString() : null;
                    try {
                        MongoDatabaseConnection storedConnection = new MongoDatabaseConnection(id, host, port, dbName, user, password);
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
    public void addConnection(Connection connection) {
        // TODO
    }
}
