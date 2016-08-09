package org.jahia.modules.databaseConnector.api.factories;

import org.glassfish.hk2.api.Factory;
import org.jahia.modules.databaseConnector.api.SpringBeansAccess;
import org.jahia.modules.databaseConnector.connection.DatabaseConnectorManager;

/**
 * @author stefan on 2016-05-04.
 */
public class DatabaseConnectorManagerFactory implements Factory<DatabaseConnectorManager> {
    @Override
    public DatabaseConnectorManager provide() {
        return SpringBeansAccess.getInstance().getDatabaseConnectorManager();
    }

    @Override
    public void dispose(DatabaseConnectorManager databaseConnectorManager) {

    }
}
