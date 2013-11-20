package org.jahia.modules.databaseConnector.mongo;

import org.jahia.modules.databaseConnector.DatabaseConnection;

/**
 * Date: 11/20/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public interface MongoDatabaseConnection extends DatabaseConnection {

    public static final String WRITE_CONCERN_KEY = "dc:writeConcern";

    public static final String WRITE_CONCERN_DEFAULT_VALUE = "SAFE";

    public String getWriteConcern();

}
