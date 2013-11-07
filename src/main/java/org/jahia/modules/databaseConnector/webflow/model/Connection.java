package org.jahia.modules.databaseConnector.webflow.model;

import org.jahia.modules.databaseConnector.ConnectionData;
import org.jahia.modules.databaseConnector.DatabaseConnection;
import org.jahia.modules.databaseConnector.DatabaseTypes;

import java.io.Serializable;

/**
 * Date: 11/5/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class Connection implements DatabaseConnection, Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String host;

    private Integer port;

    private String dbName;

    private String uri;

    private String user;

    private String password;

    private DatabaseTypes databaseType;

    public Connection(String databaseTypeName) {
        this.databaseType = DatabaseTypes.valueOf(databaseTypeName);
    }

    public Connection(ConnectionData connectionData) {
        this.id = connectionData.getId();
        this.host = connectionData.getHost();
        this.port = connectionData.getPort();
        this.dbName = connectionData.getDbName();
        this.uri = connectionData.getUri();
        this.user = connectionData.getUser();
        this.password = connectionData.getPassword();
        this.databaseType = connectionData.getDatabaseType();
    }

    public void clear() {
        this.id = null;
        this.host = null;
        this.port = null;
        this.dbName = null;
        this.uri = null;
        this.user = null;
        this.password = null;
        this.databaseType = null;
    }

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

    @Override
    public String getDisplayName() {
        return databaseType.getDisplayName();
    }
}
