package org.jahia.modules.databaseConnector;

/**
 * Date: 11/1/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public interface DatabaseConnection {

    public String getId();

    public String getHost();

    public Integer getPort();

    public String getDbName();

    public String getUri();

    public String getUser();

    public String getPassword();

    public DatabaseTypes getDatabaseType();

    public String getDisplayName();

}
