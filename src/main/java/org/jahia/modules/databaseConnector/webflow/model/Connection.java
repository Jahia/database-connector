package org.jahia.modules.databaseConnector.webflow.model;

import org.jahia.modules.databaseConnector.DatabaseConnection;
import org.jahia.modules.databaseConnector.DatabaseTypes;

import java.io.Serializable;

/**
 * Date: 11/19/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public interface Connection extends DatabaseConnection, Serializable {

    public void setId(String id);

    public void setHost(String host);

    public void setPort(Integer port);

    public void setDbName(String dbName);

    public void setUri(String uri);

    public void setUser(String user);

    public void setPassword(String password);

    public void setDatabaseType(DatabaseTypes databaseType);

    public String getOldId();
}
