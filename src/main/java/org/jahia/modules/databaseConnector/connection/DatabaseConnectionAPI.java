package org.jahia.modules.databaseConnector.connection;

import org.jahia.modules.databaseConnector.api.impl.DatabaseConnector;
import org.jahia.modules.databaseConnector.services.DatabaseConnectorService;
import org.jahia.modules.databaseConnector.util.Utils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * @author alexander karmanov on 2017-05-16.
 */
public class DatabaseConnectionAPI {
    private BundleContext context;
    private DatabaseConnector databaseConnector;

    public DatabaseConnectionAPI(Class apiClass) {
        context = FrameworkUtil.getBundle(apiClass).getBundleContext();
    }

    public BundleContext getContext() {
        return context;
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }

    public DatabaseConnector getDatabaseConnector() {
        if (databaseConnector == null) {
            databaseConnector = getConnectorReference();
        }
        return databaseConnector;
    }

    public void setDatabaseConnector(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    private DatabaseConnector getConnectorReference() {
        return (DatabaseConnector) Utils.getService(DatabaseConnectorService.class.getName(), context);
    }
}
