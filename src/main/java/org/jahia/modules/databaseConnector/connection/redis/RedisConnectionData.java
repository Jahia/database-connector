package org.jahia.modules.databaseConnector.connection.redis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jahia.modules.databaseConnector.connection.ConnectionData;

/**
 * Date: 11/19/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class RedisConnectionData extends ConnectionData {

    private Long timeout;

    private Integer weight;

    public RedisConnectionData(String id) {
        this.id = id;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @JsonIgnore
    public String getJson() {
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonRedisConnectionData = mapper.valueToTree(this);
        return jsonRedisConnectionData != null ? jsonRedisConnectionData.toString() : null;
    }
}
