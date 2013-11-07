package org.jahia.modules.databaseConnector.mongo;

import com.mongodb.*;
import org.jahia.modules.databaseConnector.AbstractDatabaseConnection;
import org.jahia.modules.databaseConnector.DatabaseTypes;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import java.net.UnknownHostException;

/**
 * Date: 11/1/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class MongoDatabaseConnection extends AbstractDatabaseConnection {

    public static final String NODE_TYPE = "dc:mongoConnection";

    private static final DatabaseTypes DATABASE_TYPE = DatabaseTypes.MONGO;

    private MongoDbFactory dbFactory;

    private MongoTemplate template;

    public MongoDatabaseConnection(String id, String host, Integer port) throws UnknownHostException {
        this(id, host, port, null, null, null);
    }

    public MongoDatabaseConnection(String id, String host, Integer port, String dbName) throws UnknownHostException {
        this(id, host, port, dbName, null, null);
    }

    public MongoDatabaseConnection(String id, String host, Integer port, String user, String password) throws UnknownHostException {
        this(id, host, port, null, user, password);
    }

    public MongoDatabaseConnection(String id, String host, Integer port, String dbName, String user, String password) throws UnknownHostException {
        super(id, host, port, dbName, user, password);
        ServerAddress serverAddress = new ServerAddress(host,(int) port);
        // TODO
        //MongoCredential credential = new MongoCredential()
        MongoClientOptions options = new MongoClientOptions.Builder().writeConcern(WriteConcern.SAFE).build();
        Mongo mongo = new MongoClient(serverAddress, options);
        dbFactory = new SimpleMongoDbFactory(mongo, dbName);
        template = new MongoTemplate(dbFactory);
    }

    public MongoDbFactory getDbFactory() {
        return dbFactory;
    }

    public MongoTemplate getTemplate() {
        return template;
    }

    @Override
    public DatabaseTypes getDatabaseType() {
        return DATABASE_TYPE;
    }
}
