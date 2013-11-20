package org.jahia.modules.databaseConnector.webflow.model.mongo;

import com.mongodb.ServerAddress;
import org.jahia.modules.databaseConnector.ConnectionData;
import org.jahia.modules.databaseConnector.mongo.MongoConnectionDataImpl;
import org.jahia.modules.databaseConnector.webflow.model.ConnectionImpl;

import static org.jahia.modules.databaseConnector.DatabaseTypes.MONGO;

/**
 * Date: 11/20/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class MongoConnectionImpl extends ConnectionImpl implements MongoConnection {

    private String writeConcern;

    public MongoConnectionImpl() {
        super(MONGO, ServerAddress.defaultPort());
        this.writeConcern = WRITE_CONCERN_DEFAULT_VALUE;
    }

    public MongoConnectionImpl(ConnectionData connectionData) {
        super(connectionData);
        MongoConnectionDataImpl mongoConnectionData = (MongoConnectionDataImpl) connectionData;
        this.writeConcern = mongoConnectionData.getWriteConcern();
    }

    @Override
    public String getWriteConcern() {
        return writeConcern;
    }

    @Override
    public void setWriteConcern(String writeConcern) {
        this.writeConcern = writeConcern;
    }
}
