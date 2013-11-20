package org.jahia.modules.databaseConnector.redis;

import org.jahia.modules.databaseConnector.DatabaseConnection;

/**
 * Date: 11/18/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public interface RedisDatabaseConnection extends DatabaseConnection {

    public static final String TIMEOUT_KEY = "dc:timeout";

    public static final String WEIGHT_KEY = "dc:weight";

    public Integer getTimeout();

    public Integer getWeight();

}
