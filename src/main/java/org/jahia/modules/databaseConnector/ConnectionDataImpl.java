package org.jahia.modules.databaseConnector;

/**
 * Date: 11/1/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class ConnectionDataImpl implements ConnectionData {

    private static final long serialVersionUID = 1L;

    private final String id;

    private final String host;

    private final Integer port;

    private final String dbName;

    private final String uri;

    private final String user;

    private final String password;

    private final DatabaseTypes databaseType;

    public ConnectionDataImpl(String id, String host, Integer port, String dbName, String uri,
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

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public String getDbName() {
        return dbName;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public DatabaseTypes getDatabaseType() {
        return databaseType;
    }

    @Override
    public String getDisplayName() {
        return databaseType.getDisplayName();
    }
}
