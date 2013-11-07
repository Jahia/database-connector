package org.jahia.modules.databaseConnector.webflow;

import org.jahia.modules.databaseConnector.ConnectionData;
import org.jahia.modules.databaseConnector.DatabaseConnectorManager;
import org.jahia.modules.databaseConnector.DatabaseTypes;
import org.jahia.modules.databaseConnector.webflow.model.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.*;

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
        return new Connection(databaseTypeName);
    }

    public void initDatabaseId(Connection connection) {
        if (connection.getId() == null) {
            String id = databaseConnectorManager.getNextAvailableId(connection.getDatabaseType());
            connection.setId(id);
        }
    }

    public Connection getConnection(String databaseId, String databaseTypeName) {
        return new Connection(databaseConnectorManager.getConnectionData(databaseTypeName, databaseId));
    }

    public void addConnection(Connection connection) {
        databaseConnectorManager.addConnection(connection);
    }

//    public boolean addEditNeo4jConnection(Connection connection) {
//        return databaseConnectorManager.addEditNeo4jConnection(connection);
//    }

}
