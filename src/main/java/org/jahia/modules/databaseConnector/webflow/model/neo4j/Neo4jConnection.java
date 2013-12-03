package org.jahia.modules.databaseConnector.webflow.model.neo4j;

import org.jahia.modules.databaseConnector.ConnectionData;
import org.jahia.modules.databaseConnector.webflow.model.AbstractConnection;

import static org.jahia.modules.databaseConnector.DatabaseTypes.NEO4J;

/**
 * Date: 11/26/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class Neo4jConnection extends AbstractConnection {
    
    public Neo4jConnection() {
        super(NEO4J);
        this.uri = "http://localhost:7474/db/data/";
    }
    
    public Neo4jConnection(ConnectionData connectionData) {
        super(connectionData);
    }

    @Override
    public void validateEnterConfig() {
        if (uri == null || uri.isEmpty()) {
            addRequiredErrorMessage("uri");
        }
    }
}
