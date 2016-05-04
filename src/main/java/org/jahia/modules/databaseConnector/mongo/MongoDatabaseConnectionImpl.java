package org.jahia.modules.databaseConnector.mongo;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import org.jahia.modules.databaseConnector.AbstractDatabaseConnection;
import org.jahia.modules.databaseConnector.ConnectionData;
import org.jahia.modules.databaseConnector.DatabaseTypes;

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

    private MongoDatabase databaseConnection;

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
        MongoClientOptions options = new MongoClientOptions.Builder().writeConcern(WriteConcern.valueOf(this.writeConcern)).build();
        if (credential != null) {
            ArrayList<MongoCredential> mongoCredentials = new ArrayList<MongoCredential>(1);
            mongoCredentials.add(credential);
            databaseConnection = (new MongoClient(serverAddress, options)).getDatabase(dbName);
        }
        else {
            databaseConnection = (new MongoClient(serverAddress, options)).getDatabase(dbName);
        }
    }

    @Override
    public ConnectionData makeConnectionData() {
        return new MongoConnectionDataImpl(id, host, port, dbName, uri, user, password, getDatabaseType(), writeConcern);
    }

    @Override
    protected boolean registerAsService() {
        return registerAsService(databaseConnection);
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
