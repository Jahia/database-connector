package org.jahia.modules.databaseConnector.serialization.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jahia.modules.databaseConnector.connection.mongo.MongoConnectionData;

import java.util.List;

/**
 * Created by stefan on 2016-05-09.
 */
public class MongoDbConnections {
    List<MongoConnectionData> connections;

    //Default constructor used by JSON deserializer
    public MongoDbConnections() {

    }

    public MongoDbConnections(List <MongoConnectionData> connections) {
        this.connections = connections;
    }

    @JsonIgnore
    public String getJson() {
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonMongoDbConnections = mapper.valueToTree(this);
        return jsonMongoDbConnections != null ? jsonMongoDbConnections.toString() : null;
    }

    public List<MongoConnectionData> getConnections() {
        return connections;
    }

    public void setConnections(List<MongoConnectionData> connections) {
        this.connections = connections;
    }

}
