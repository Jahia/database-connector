package org.jahia.modules.databaseConnector.webflow;

import org.jahia.modules.databaseConnector.ConnectionData;
import org.jahia.modules.databaseConnector.DatabaseConnectorManager;
import org.jahia.modules.databaseConnector.DatabaseTypes;
import org.jahia.modules.databaseConnector.webflow.model.Connection;
import org.jahia.modules.databaseConnector.webflow.model.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Date: 11/1/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class DatabaseConnectorFlowHandler implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectorFlowHandler.class);

    @Autowired
    private transient DatabaseConnectorManager databaseConnectorManager;

    public Map<DatabaseTypes, Set<ConnectionData>> findRegisteredConnections() {
        return databaseConnectorManager.findRegisteredConnections();
    }

    public Map<DatabaseTypes, Map<String, Object>> findAllDatabaseTypes() {
        return databaseConnectorManager.findAllDatabaseTypes();
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

    public void addEditConnection(Connection connection, boolean isEdition) {
        databaseConnectorManager.addEditConnection(connection, isEdition);
    }

    public void removeConnection(String databaseId, String databaseTypeName) {
        databaseConnectorManager.removeConnection(databaseId, databaseTypeName);
    }
}
