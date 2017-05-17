package org.jahia.modules.databaseConnector.connection;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
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
    private final static String DATABASE_ID_KEY = "databaseId";
    public final static String DATABASE_TYPE_KEY = "databaseType";
    private final List<ServiceRegistration> serviceRegistrations = new LinkedList<>();
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

    public static String createFilter(String databaseType, String databaseId) {
        return "(&(" + DATABASE_TYPE_KEY + "=" + databaseType + ")(" + DATABASE_ID_KEY + "=" + databaseId + "))";
    }

    public static String createSingleFilter(String databaseType) {
        return "(" + DATABASE_TYPE_KEY + "=" + databaseType + ")";
    }

    public abstract boolean testConnectionCreation();

    private Hashtable<String, String> createProperties(String databaseType, String databaseId) {
        Hashtable<String, String> properties = new Hashtable<>();
        properties.put(DATABASE_TYPE_KEY, databaseType);
        properties.put(DATABASE_ID_KEY, databaseId);
        return properties;
    }

    private String[] getInterfacesNames(Object obj) {
        Class[] interfaces = obj.getClass().getInterfaces();
        String[] interfacesNames = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            interfacesNames[i] = interfaces[i].getName();
        }
        return interfacesNames;
    }

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

    public abstract String getDatabaseType();

    public abstract  String getDisplayName();

    public abstract String getSerializedExportData();

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public abstract String parseOptions(LinkedHashMap<String, Object> options);

    public abstract T makeConnectionData();

    public abstract E getServerStatus();

    public abstract Object establishConnection();

    public abstract void forgetConnection();

    public abstract Object getClient(String connectionId);
}

