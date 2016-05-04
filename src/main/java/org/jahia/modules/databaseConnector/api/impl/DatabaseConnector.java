package org.jahia.modules.databaseConnector.api.impl;


import org.jahia.modules.databaseConnector.DatabaseConnectorManager;
import org.jahia.modules.databaseConnector.webflow.model.Connection;
import org.jahia.modules.databaseConnector.webflow.model.ConnectionFactory;
import org.jahia.services.content.JCRTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by donnylam on 2016-05-04.
 */
public class DatabaseConnector extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(DatabaseConnector.class);

    private transient DatabaseConnectorManager databaseConnectorManager;

    public DatabaseConnector(JCRTemplate jcrTemplate, Logger logger) {
        super(jcrTemplate, logger);
    }

    public Connection initConnection(String databaseTypeName) {
        Connection connection = ConnectionFactory.makeConnection(databaseTypeName);
        String id = databaseConnectorManager.getNextAvailableId(connection.getDatabaseType());
        connection.setId(id);
        return connection;
    }
}
