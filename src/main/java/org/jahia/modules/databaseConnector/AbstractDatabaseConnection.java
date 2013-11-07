package org.jahia.modules.databaseConnector;

/**
 * Date: 11/1/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public abstract class AbstractDatabaseConnection implements DatabaseConnection, Comparable<AbstractDatabaseConnection> {

    private String id;

    private String host;

    private Integer port;

    private String dbName;

    private String uri;

    private String user;

    private String password;

    public AbstractDatabaseConnection(String id, String uri) {
        this(id, null, null, null, uri, null, null);
    }

    public AbstractDatabaseConnection(String id, String uri, String user, String password) {
        this(id, null, null, null, uri, user, password);
    }

    public AbstractDatabaseConnection(String id, String host, Integer port, String dbName) {
        this(id, host, port, dbName, null, null, null);
    }

    public AbstractDatabaseConnection(String id, String host, Integer port, String dbName, String user, String password) {
        this(id, host, port, dbName, null, user, password);
    }

    public AbstractDatabaseConnection(String id, String host, Integer port, String dbName, String uri, String user, String password) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.dbName = dbName;
        this.uri = uri;
        this.user = user;
        this.password = password;
    }

    public ConnectionData createData(){
        return new ConnectionData(id, host, port, dbName, uri, user, password, getDatabaseType());
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

    public String getUri() {
        return uri;
    }

    @Override
    public String getDbName() {
        return dbName;
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
    public String getDisplayName() {
        return getDatabaseType().getDisplayName();
    }

    @Override
    public int compareTo(AbstractDatabaseConnection anotherDatabaseConnection) {
        return this.getId().compareTo(anotherDatabaseConnection.getId());
    }
}
