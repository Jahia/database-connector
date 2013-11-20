package org.jahia.modules.databaseConnector.redis;

import org.jahia.modules.databaseConnector.ConnectionDataImpl;
import org.jahia.modules.databaseConnector.DatabaseTypes;

/**
 * Date: 11/19/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class RedisConnectionDataImpl extends ConnectionDataImpl implements RedisDatabaseConnection {

    private Integer timeout;

    private Integer weight;

    public RedisConnectionDataImpl(String id, String host, Integer port, String dbName, String uri,
                                   String user, String password, DatabaseTypes databaseType, Integer timeout, Integer weight) {
        super(id, host, port, dbName, uri, user, password, databaseType);
        this.timeout = timeout;
        this.weight = weight;
    }

    @Override
    public Integer getTimeout() {
        return timeout;
    }

    @Override
    public Integer getWeight() {
        return weight;
    }
}
