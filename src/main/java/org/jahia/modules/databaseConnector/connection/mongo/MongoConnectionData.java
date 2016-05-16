package org.jahia.modules.databaseConnector.connection.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jahia.modules.databaseConnector.connection.ConnectionData;

public class MongoConnectionData extends ConnectionData {

    private String writeConcern;

    private String authDb;

    public MongoConnectionData(String id) {
        this.id = id;
    }

    public String getWriteConcern() {
        return this.writeConcern;
    }

    public void setWriteConcern(String writeConcern) {
        this.writeConcern = writeConcern;
    }

    public String getAuthDb() {
        return this.authDb;
    }

    public void setAuthDb(String authDb) {
        this.authDb = authDb;
    }

    @JsonIgnore
    public String getJson() {
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonMongoConnectionData = mapper.valueToTree(this);
        return jsonMongoConnectionData != null ? jsonMongoConnectionData.toString() : null;
    }
}
