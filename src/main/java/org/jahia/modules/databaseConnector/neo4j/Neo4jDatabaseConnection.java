package org.jahia.modules.databaseConnector.neo4j;

import org.jahia.modules.databaseConnector.AbstractDatabaseConnection;
import org.jahia.modules.databaseConnector.DatabaseTypes;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.data.neo4j.template.Neo4jTemplate;

/**
 * Date: 11/1/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class Neo4jDatabaseConnection extends AbstractDatabaseConnection {

    public static final String NODE_TYPE = "dc:neo4jConnection";

    private static final DatabaseTypes DATABASE_TYPE = DatabaseTypes.NEO4J;

    private Configuration neo4JConfig;

    public Neo4jDatabaseConnection(String id, String uri) {
        this(id, uri, null, null);
    }

    public Neo4jDatabaseConnection(String id, String uri, String user, String password) {
        super(id, uri, user, password);
        neo4JConfig = new Configuration();
        neo4JConfig
                .driverConfiguration()
                .setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver")
                .setCredentials(user, password)
                .setURI(uri);
        SessionFactory sessionFactory = new SessionFactory(neo4JConfig, "org.jahia.modules");
    }

    @Override
    public boolean registerAsService() {
        return registerAsService(neo4JConfig);
    }

    public Configuration getNeo4JConfig() {
        return neo4JConfig;
    }

    @Override
    public DatabaseTypes getDatabaseType() {
        return DATABASE_TYPE;
    }
}
