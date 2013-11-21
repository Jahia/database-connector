package org.jahia.modules.databaseConnector.mongo;

import com.mongodb.*;
import org.jahia.modules.databaseConnector.AbstractDatabaseConnection;
import org.jahia.modules.databaseConnector.ConnectionData;
import org.jahia.modules.databaseConnector.DatabaseTypes;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Date: 11/1/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class MongoDatabaseConnectionImpl extends AbstractDatabaseConnection implements MongoDatabaseConnection {

    public static final String NODE_TYPE = "dc:mongoConnection";

    private static final DatabaseTypes DATABASE_TYPE = DatabaseTypes.MONGO;

    private MongoDbFactory dbFactory;

    private MongoTemplate template;

    private final String writeConcern;

    public MongoDatabaseConnectionImpl(String id, String host, Integer port) throws UnknownHostException {
        this(id, host, port, null, null, null, null);
    }

    public MongoDatabaseConnectionImpl(String id, String host, Integer port, String dbName) throws UnknownHostException {
        this(id, host, port, dbName, null, null, null);
    }

    public MongoDatabaseConnectionImpl(String id, String host, Integer port, String dbName, String user, String password) throws UnknownHostException {
        this(id, host, port, dbName, user, password, null);
    }

    public MongoDatabaseConnectionImpl(String id, String host, Integer port, String dbName, String user, String password, String writeConcern) throws UnknownHostException {
        super(id, host, port, dbName, user, password);
        if (writeConcern == null) {
            this.writeConcern = WRITE_CONCERN_DEFAULT_VALUE;
        }
        else {
            this.writeConcern = writeConcern;
        }
        ServerAddress serverAddress = new ServerAddress(host, port);
        MongoCredential credential = null;
        if (user != null) {
            if (password != null) {
                credential = MongoCredential.createMongoCRCredential(user, dbName, password.toCharArray());
            }
            else {
                credential = MongoCredential.createGSSAPICredential(user);
            }
        }
        MongoClientOptions options = new MongoClientOptions.Builder().writeConcern(WriteConcern.valueOf(writeConcern)).build();
        Mongo mongo;
        if (credential != null) {
            ArrayList<MongoCredential> mongoCredentials = new ArrayList<MongoCredential>(1);
            mongoCredentials.add(credential);
            mongo = new MongoClient(serverAddress, mongoCredentials , options);
            UserCredentials userCredentials = new UserCredentials(user, password);
            dbFactory = new SimpleMongoDbFactory(mongo, dbName, userCredentials);
            template = new MongoTemplate(dbFactory);
        }
        else {
            mongo = new MongoClient(serverAddress, options);
            dbFactory = new SimpleMongoDbFactory(mongo, dbName);
            template = new MongoTemplate(dbFactory);
        }
    }

    @Override
    public ConnectionData makeConnectionData() {
        return new MongoConnectionDataImpl(id, host, port, dbName, uri, user, password, getDatabaseType(), writeConcern);
    }

    @Override
    protected boolean registerAsService() {
        boolean b = registerAsService(template);
        boolean b1 = registerAsService(dbFactory, true);
        return b && b1;
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

    @Override
    public String getWriteConcern() {
        return writeConcern;
    }
}
