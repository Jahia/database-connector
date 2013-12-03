package org.jahia.modules.databaseConnector.webflow;

import org.jahia.modules.databaseConnector.ConnectionData;
import org.jahia.modules.databaseConnector.DatabaseConnectorManager;
import org.jahia.modules.databaseConnector.DatabaseTypes;
import org.jahia.modules.databaseConnector.webflow.model.Connection;
import org.jahia.modules.databaseConnector.webflow.model.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import static org.jahia.modules.databaseConnector.Utils.getMessage;

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

    public boolean addEditConnection(Connection connection, Boolean isEdition) {
        return databaseConnectorManager.addEditConnection(connection, isEdition);
    }

    public boolean removeConnection(String databaseId, String databaseTypeName) {
        return databaseConnectorManager.removeConnection(databaseId, databaseTypeName);
    }

    public String checkRemoveOperationResult(RequestContext context) {
        if (context.getFlashScope().contains("removeOperationResult")
                && (Boolean) context.getFlashScope().get("removeOperationResult")) {
            return "success";
        }
        else {
            String messageArg = getMessage("dc_databaseConnector.alert.errorAlert.removing");
            String message = getMessage("dc_databaseConnector.alert.errorAlert", messageArg);
            context.getFlashScope().put("errorMessage", message);
            return "failed";
        }
    }

    public String checkAddEditOperationResult(RequestContext context, Boolean isEdition) {
        if (context.getFlashScope().contains("addEditOperationResult")
                && (Boolean) context.getFlashScope().get("addEditOperationResult")) {
            return "success";
        }
        else {
            String messageArg = getMessage("dc_databaseConnector.alert.errorAlert."+ (isEdition ? "editing" : "adding"));
            String message = getMessage("dc_databaseConnector.alert.errorAlert", messageArg);
            context.getFlashScope().put("errorMessage", message);
            return "failed";
        }
    }
}
