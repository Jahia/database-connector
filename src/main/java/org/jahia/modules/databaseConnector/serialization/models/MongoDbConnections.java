package org.jahia.modules.databaseConnector.serialization.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jahia.modules.databaseConnector.mongo.MongoConnectionDataImpl;

import java.util.List;

/**
 * Created by stefan on 2016-05-09.
 */
public class MongoDbConnections {
    List<MongoConnectionDataImpl> connections;

    //Default constructor used by JSON deserializer
    public MongoDbConnections() {

    }

    public MongoDbConnections(List <MongoConnectionDataImpl> connections) {
        this.connections = connections;
    }

    @JsonIgnore
    public String getJson() {
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonMongoDbConnections = mapper.valueToTree(this);
        return jsonMongoDbConnections != null ? jsonMongoDbConnections.toString() : null;
    }

    public List<MongoConnectionDataImpl> getConnections() {
        return connections;
    }

    public void setConnections(List<MongoConnectionDataImpl> connections) {
        this.connections = connections;
    }

}
