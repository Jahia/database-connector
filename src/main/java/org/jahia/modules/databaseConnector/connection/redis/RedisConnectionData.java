package org.jahia.modules.databaseConnector.connection.redis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jahia.modules.databaseConnector.connection.DatabaseTypes;
import org.jahia.modules.databaseConnector.connection.ConnectionData;

/**
 * Date: 11/19/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class RedisConnectionData extends ConnectionData{

    private final Integer timeout;

    private final Integer weight;

    public RedisConnectionData(String id, String host, Integer port, String dbName, String uri,
                                   String user, String password, DatabaseTypes databaseType, Integer timeout, Integer weight) {
        super(id, host, port, dbName, uri, user, password, databaseType);
        this.timeout = timeout;
        this.weight = weight;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public Integer getWeight() {
        return weight;
    }

    @JsonIgnore
    public String getJson() {
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonRedisConnectionData = mapper.valueToTree(this);
        return jsonRedisConnectionData != null ? jsonRedisConnectionData.toString() : null;
    }
}
