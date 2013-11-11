package org.jahia.modules.databaseConnector.neo4j;

import org.jahia.modules.databaseConnector.AbstractDatabaseConnection;
import org.jahia.modules.databaseConnector.DatabaseTypes;
import org.springframework.data.neo4j.rest.SpringRestGraphDatabase;

/**
 * Date: 11/1/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class Neo4jDatabaseConnection extends AbstractDatabaseConnection {

    public static final String NODE_TYPE = "dc:neo4jConnection";

    private static final DatabaseTypes DATABASE_TYPE = DatabaseTypes.NEO4J;

    private SpringRestGraphDatabase graphDatabaseService;

    public Neo4jDatabaseConnection(String id, String uri) {
        this(id, uri, null, null);
    }

    public Neo4jDatabaseConnection(String id, String uri, String user, String password) {
        super(id, uri, user, password);
        this.graphDatabaseService = new SpringRestGraphDatabase(uri, user, password);
    }

    @Override
    public boolean registerAsService() {
        return registerAsService(graphDatabaseService);
    }

    public SpringRestGraphDatabase getGraphDatabaseService() {
        return graphDatabaseService;
    }

    @Override
    public DatabaseTypes getDatabaseType() {
        return DATABASE_TYPE;
    }
}
