package org.jahia.modules.databaseConnector.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jahia.modules.databaseConnector.ConnectionDataImpl;
import org.jahia.modules.databaseConnector.DatabaseTypes;

/**
 * Date: 11/20/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class MongoConnectionDataImpl extends ConnectionDataImpl implements MongoDatabaseConnection {

    private final String writeConcern;

    public MongoConnectionDataImpl(String id, String host, Integer port, String dbName, String uri,
                                   String user, String password, DatabaseTypes databaseType, String writeConcern) {
        super(id, host, port, dbName, uri, user, password, databaseType);
        this.writeConcern = writeConcern;
    }

    @Override
    public String getWriteConcern() {
        return writeConcern;
    }

    @JsonIgnore
    public String getJson() {
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonForm = mapper.valueToTree(this);
        return jsonForm != null ? jsonForm.toString() : null;
    }
}
