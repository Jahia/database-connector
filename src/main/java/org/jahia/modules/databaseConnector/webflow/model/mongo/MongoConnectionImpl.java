package org.jahia.modules.databaseConnector.webflow.model.mongo;

import com.mongodb.ServerAddress;
import org.jahia.modules.databaseConnector.ConnectionData;
import org.jahia.modules.databaseConnector.mongo.MongoConnectionDataImpl;
import org.jahia.modules.databaseConnector.webflow.model.AbstractConnection;

import static org.jahia.modules.databaseConnector.DatabaseTypes.MONGO;

/**
 * Date: 11/20/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class MongoConnectionImpl extends AbstractConnection implements MongoConnection {

    private String writeConcern;

    public MongoConnectionImpl() {
        super(MONGO);
        this.host = ServerAddress.defaultHost();
        this.port = ServerAddress.defaultPort();
        this.dbName = "test";
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

    @Override
    public void validateEnterConfig() {
        if (host == null || host.isEmpty()) {
            addRequiredErrorMessage("host");
        }
        if (port == null) {
            addRequiredErrorMessage("port");
        } else if (port <= 0) {
            addErrorMessage("port", "dc_databaseConnector.label.port.message.askPositiveInteger");
        }
        if (dbName == null || dbName.isEmpty()) {
            addRequiredErrorMessage("dbName");
        }
    }
}
