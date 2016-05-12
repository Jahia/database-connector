package org.jahia.modules.databaseConnector.connection.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jahia.modules.databaseConnector.connection.ConnectionData;
import static org.jahia.modules.databaseConnector.connection.DatabaseTypes.MONGO;

public class MongoConnectionData extends ConnectionData {

    private String writeConcern;

    private String authDb;

    public MongoConnectionData(String id, String host, Integer port, Boolean isConnected, String dbName,
                               String uri, String user, String password, String authDb, String writeConcern) {
        super(id, host, port, isConnected, dbName, uri, user, password, MONGO);
        this.writeConcern = writeConcern;
        this.authDb = authDb;
    }

    public String getWriteConcern() {
        return this.writeConcern;
    }

    public String getAuthDb() {
        return this.authDb;
    }

    @JsonIgnore
    public String getJson() {
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonMongoConnectionData = mapper.valueToTree(this);
        return jsonMongoConnectionData != null ? jsonMongoConnectionData.toString() : null;
    }
}
