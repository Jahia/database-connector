package org.jahia.modules.databaseConnector.webflow.model;

import org.jahia.modules.databaseConnector.ConnectionData;
import org.jahia.modules.databaseConnector.DatabaseTypes;

/**
 * Date: 11/5/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class ConnectionImpl implements Connection {

    private static final long serialVersionUID = 1L;

    private String id;

    private String oldId;

    private String host;

    private Integer port;

    private String dbName;

    private String uri;

    private String user;

    private String password;

    private DatabaseTypes databaseType;

    public ConnectionImpl(String databaseTypeName) {
        this.databaseType = DatabaseTypes.valueOf(databaseTypeName);
    }

    public ConnectionImpl(DatabaseTypes databaseType) {
        this.databaseType = databaseType;
    }

    protected ConnectionImpl(DatabaseTypes databaseType, Integer defaultPort) {
        this.databaseType = databaseType;
        this.port = defaultPort;
    }

    public ConnectionImpl(ConnectionData connectionData) {
        this.id = connectionData.getId();
        this.oldId = id;
        this.host = connectionData.getHost();
        this.port = connectionData.getPort();
        this.dbName = connectionData.getDbName();
        this.uri = connectionData.getUri();
        this.user = connectionData.getUser();
        this.password = connectionData.getPassword();
        this.databaseType = connectionData.getDatabaseType();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getOldId() {
        return oldId;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String getDbName() {
        return dbName;
    }

    @Override
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public DatabaseTypes getDatabaseType() {
        return databaseType;
    }

    @Override
    public void setDatabaseType(DatabaseTypes databaseType) {
        this.databaseType = databaseType;
    }

    @Override
    public String getDisplayName() {
        return databaseType.getDisplayName();
    }
}
