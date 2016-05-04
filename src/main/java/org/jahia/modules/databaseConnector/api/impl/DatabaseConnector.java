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

    public DatabaseConnector(JCRTemplate jcrTemplate, DatabaseConnectorManager databaseConnectorManager, Logger logger) {
        super(jcrTemplate, logger);
        this.databaseConnectorManager = databaseConnectorManager;
    }

    public Connection initConnection(String databaseTypeName) {
        Connection connection = ConnectionFactory.makeConnection(databaseTypeName);
        String id = databaseConnectorManager.getNextAvailableId(connection.getDatabaseType());
        connection.setId(id);
        return connection;
    }


    public Connection getConnection(String databaseId, String databaseTypeName) {

        return ConnectionFactory.makeConnection(databaseConnectorManager.getConnectionData(databaseId, databaseTypeName));
    }


    public boolean addEditConnection(Connection connection, Boolean isEdition) {
        return databaseConnectorManager.addEditConnection(connection, isEdition);
    }


    public boolean removeConnection(String databaseId, String databaseTypeName) {
        return databaseConnectorManager.removeConnection(databaseId, databaseTypeName);
    }
}
