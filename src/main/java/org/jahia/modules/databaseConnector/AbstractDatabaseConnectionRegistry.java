package org.jahia.modules.databaseConnector;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.Map;
import java.util.TreeMap;

import static org.jahia.modules.databaseConnector.DatabaseConnectorManager.*;

/**
 * Date: 11/6/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public abstract class AbstractDatabaseConnectionRegistry<T extends AbstractDatabaseConnection> implements DatabaseConnectionRegistry<T> {

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
            return settings.addNode(DATABASE_CONNECTOR_PATH, "dc:databaseConnector");
        }
    }
}
