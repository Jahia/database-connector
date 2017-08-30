package org.jahia.modules.databaseConnector.util;

import org.jahia.services.content.JCRSessionWrapper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;

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

    public static Object getService(Class classObj, BundleContext context) {
        return getService(classObj.getName(), context);
    }

    public static Object getService(String className, BundleContext context) {
        ServiceReference ref = context.getServiceReference(className);
        return ref != null ? context.getService(ref) : null;
    }

    public static String resolveBundleName(int code) {
        switch(code) {
            case BundleEvent.INSTALLED:
               return "INSTALLED";
            case BundleEvent.LAZY_ACTIVATION:
                return "LAZY_ACTIVATION";
            case BundleEvent.RESOLVED:
                return "RESOLVED";
            case BundleEvent.STARTED:
                return "STARTED";
            case BundleEvent.STARTING:
                return "STARTING";
            case BundleEvent.STOPPED:
                return "STARTING";
            case BundleEvent.STOPPING:
                return "STOPPING";
            case BundleEvent.UNINSTALLED:
                return "UNINSTALLED";
            case BundleEvent.UNRESOLVED:
                return "UNRESOLVED";
            case BundleEvent.UPDATED:
                return "UPDATED";
            default:
                return "" + code;
        }
    }

}
