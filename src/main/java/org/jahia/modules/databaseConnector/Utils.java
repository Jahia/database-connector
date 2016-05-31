package org.jahia.modules.databaseConnector;

import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.utils.i18n.Messages;
import org.springframework.context.i18n.LocaleContextHolder;

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

//    public final static char NEW_LINE = System.newLine();
    public final static char WHITE_SPACE = '\u0020';
    public final static char TABU = '\u0009';
    public static QueryResult query(String statement, JCRSessionWrapper session) throws RepositoryException {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query query = queryManager.createQuery(statement, Query.JCR_SQL2);
        return query.execute();
    }
}
