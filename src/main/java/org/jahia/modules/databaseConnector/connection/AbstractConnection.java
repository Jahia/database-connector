package org.jahia.modules.databaseConnector.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/**
 * @author stefan on 2016-05-10.
 */

public abstract class AbstractConnection<T extends ConnectionData, E extends Object> implements Serializable {

    public final static String ID_KEY = "dc:id";
    public final static String HOST_KEY = "dc:host";
    public final static String PORT_KEY = "dc:port";
    public final static String DB_NAME_KEY = "dc:dbName";
    public final static String URI_KEY = "dc:uri";
    public final static String USER_KEY = "dc:user";
    public final static String PASSWORD_KEY = "dc:password";
    public final static String IS_CONNECTED_KEY = "dc:isConnected";
    public final static String OPTIONS_KEY = "dc:options";
    private static final Logger logger = LoggerFactory.getLogger(AbstractConnection.class);
    private static final long serialVersionUID = 1L;
    protected String id;
    protected String oldId;
    protected String host;
    protected Integer port;
    protected String dbName;
    protected String uri;
    protected String user;
    protected String password;
    protected String options;
    protected Boolean isConnected;

    public abstract boolean testConnectionCreation();
    public abstract Object beforeRegisterAsService();
    public abstract void beforeUnregisterAsService();
    public abstract String parseOptions(LinkedHashMap<String, Object> options);
    public abstract T makeConnectionData();
    public abstract E getServerStatus();
    public abstract Object establishConnection();
    public abstract void forgetConnection();
    public abstract String getNodeType();
    public abstract String getDatabaseType();
    public abstract  String getDisplayName();
    public abstract String getSerializedExportData();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOldId() {
        return oldId;
    }

    public void setOldId(String oldId) {
        this.oldId = oldId;
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

    public Boolean isConnected() {
        return isConnected;
    }

    public void isConnected(Boolean connected) {
        isConnected = connected;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }
}

