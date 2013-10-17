package org.jahia.modules.databaseConnector;

import org.osgi.framework.InvalidSyntaxException;

/**
 * Date: 2013-10-17
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public interface DatabaseConnectorOSGiService {

    boolean registerNeo4jGraphDatabase() throws InvalidSyntaxException;

    boolean registerRedisConnectionFactory() throws InvalidSyntaxException;

    boolean registerRedisStringTemplate() throws InvalidSyntaxException;

    boolean registerRedisLongTemplate() throws InvalidSyntaxException;

    boolean registerRedisIntegerTemplate() throws InvalidSyntaxException;
}
