package org.jahia.modules.databaseConnector;

import org.osgi.framework.InvalidSyntaxException;

/**
 * Date: 2013-10-17
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public interface DatabaseConnectorOSGiService {

    boolean registerNeo4jGraphDatabase(String databaseId) throws InvalidSyntaxException;

    boolean registerRedisConnectionFactory(String databaseId) throws InvalidSyntaxException;

    boolean registerRedisStringTemplate(String databaseId) throws InvalidSyntaxException;

    boolean registerRedisLongTemplate(String databaseId) throws InvalidSyntaxException;

    boolean registerRedisIntegerTemplate(String databaseId) throws InvalidSyntaxException;

    boolean registerMongoTemplate(String databaseId) throws InvalidSyntaxException;

    boolean registerMongoDbFactory(String databaseId) throws InvalidSyntaxException;
}
