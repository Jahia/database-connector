package org.jahia.modules.databaseConnector;

/**
 * Date: 2013-10-17
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public interface DatabaseConnectorOSGiService {

    public static final String DATABASE_CONNECTOR_ROOT_PATH = "/settings/";

    public static final String DATABASE_CONNECTOR_PATH = "databaseConnector";

    public static final String DATABASE_CONNECTOR_NODE_TYPE = "dc:databaseConnector";

    boolean registerSingleDatabase(DatabaseTypes databaseType);

    boolean registerDatabase(String databaseId, DatabaseTypes databaseType);
}
