package org.jahia.modules.databaseConnector.connection;

import java.io.Serializable;

/**
 * Date: 11/1/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public abstract class ConnectionData implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String id;

    protected String host;

    protected Integer port;

    protected Boolean isConnected;

    protected String dbName;

    protected String uri;

    protected String user;

    protected String password;

    protected DatabaseTypes databaseType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Boolean getIsConnected() {
        return isConnected;
    }

    public void isConnected(Boolean connected) {
        isConnected = connected;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DatabaseTypes getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseTypes databaseType) {
        this.databaseType = databaseType;
    }

    public String getDisplayName() {
        return databaseType.getDisplayName();
    }
}
