package org.jahia.modules.databaseConnector.serialization.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jahia.modules.databaseConnector.connection.ConnectionData;

import java.util.List;

/**
 * Created by stefan on 2016-05-09.
 */
public class DbConnections<T extends ConnectionData> {
    List<T> connections;

    //Default constructor used by JSON deserializer
    public DbConnections() {

    }

    public DbConnections(List <T> connections) {
        this.connections = connections;
    }

    @JsonIgnore
    public String getJson() {
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonMongoDbConnections = mapper.valueToTree(this);
        return jsonMongoDbConnections != null ? jsonMongoDbConnections.toString() : null;
    }

    public List<T> getConnections() {
        return connections;
    }

    public void setConnections(List<T> connections) {
        this.connections = connections;
    }

}
