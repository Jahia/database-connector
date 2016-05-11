package org.jahia.modules.databaseConnector.connection.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jahia.modules.databaseConnector.connection.ConnectionData;
import static org.jahia.modules.databaseConnector.connection.DatabaseTypes.MONGO;

public class MongoConnectionData extends ConnectionData {

    private String writeConcern;

    public static final String WRITE_CONCERN_KEY = "dc:writeConcern";

    public static final String WRITE_CONCERN_DEFAULT_VALUE = "SAFE";

    public MongoConnectionData(String id, String host, Integer port, String dbName,
                               String uri, String user, String password, String writeConcern) {
        super(id, host, port, dbName, uri, user, password, MONGO);
        this.writeConcern = writeConcern;
    }

    public String getWriteConcern() {
        return this.writeConcern;
    }

    @JsonIgnore
    public String getJson() {
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonMongoConnectionData = mapper.valueToTree(this);
        return jsonMongoConnectionData != null ? jsonMongoConnectionData.toString() : null;
    }
}
