package org.jahia.modules.databaseConnector.util;

import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

/**
 * Date: 12/3/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class Utils {
    public final static String NEW_LINE = System.getProperty("line.separator");//This will retrieve line separator dependent on OS.
    public final static char TABU = '\u0009';
    public final static char DOUBLE_QUOTE = '\u0022';

    public static QueryResult query(String statement, JCRSessionWrapper session) throws RepositoryException {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query query = queryManager.createQuery(statement, Query.JCR_SQL2);
        return query.execute();
    }
}
