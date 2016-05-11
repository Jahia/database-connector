package org.jahia.modules.databaseConnector.connection;

import java.io.Serializable;

/**
 * Date: 11/1/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class ConnectionData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;

    private final String host;

    private final Integer port;

    private final String dbName;

    private final String uri;

    private final String user;

    private final String password;

    private final DatabaseTypes databaseType;

    public ConnectionData(String id, String host, Integer port, String dbName, String uri,
                          String user, String password, DatabaseTypes databaseType) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.dbName = dbName;
        this.uri = uri;
        this.user = user;
        this.password = password;
        this.databaseType = databaseType;
    }

    public String getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getDbName() {
        return dbName;
    }

    public String getUri() {
        return uri;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public DatabaseTypes getDatabaseType() {
        return databaseType;
    }

    public String getDisplayName() {
        return databaseType.getDisplayName();
    }
}
