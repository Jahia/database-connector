package org.jahia.modules.databaseConnector.webflow.model.redis;

import org.jahia.modules.databaseConnector.redis.RedisDatabaseConnection;

/**
 * Date: 11/20/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public interface RedisConnection extends RedisDatabaseConnection {

    public void setTimeout(Integer timeout);

    public void setWeight(Integer weight);

}
