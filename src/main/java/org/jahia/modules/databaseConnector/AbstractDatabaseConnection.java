package org.jahia.modules.databaseConnector;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Date: 11/1/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public abstract class AbstractDatabaseConnection implements DatabaseConnection, ConnectionDataFactory, Comparable<AbstractDatabaseConnection> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDatabaseConnection.class);

    protected String id;

    public final static String ID_KEY = "dc:id";

    protected String host;

    public final static String HOST_KEY = "dc:host";

    protected Integer port;

    public final static String PORT_KEY = "dc:port";

    protected String dbName;

    public final static String DB_NAME_KEY = "dc:dbName";

    protected String uri;

    public final static String URI_KEY = "dc:uri";

    protected String user;

    public final static String USER_KEY = "dc:user";

    protected String password;

    public final static String PASSWORD_KEY = "dc:password";

    private final static String DATABASE_ID_KEY = "databaseId";

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

    @Override
    public ConnectionData makeConnectionData() {
        return new ConnectionDataImpl(id, host, port, dbName, uri, user, password, getDatabaseType());
    }

    protected abstract boolean registerAsService();

    protected boolean registerAsService(Object object) {
        return registerAsService(object, false);
    }

    protected boolean registerAsService(Object object, boolean withInterfaceNames) {
        String[] messageArgs = {object.getClass().getSimpleName(), getDatabaseType().getDisplayName(), id};
        logger.info("Start registering OSGi service for {} for DatabaseConnection of type {} with id '{}'", messageArgs);
        ServiceReference[] serviceReferences = null;
        BundleContext bundleContext = DatabaseConnectorManager.getInstance().getBundleContext();
        try {
            serviceReferences = bundleContext.getAllServiceReferences(object.getClass().getName(), createFilter(getDatabaseType(), id));
        } catch (InvalidSyntaxException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        if (serviceReferences != null) {
            logger.info("OSGi service for {} already registered for DatabaseConnection of type {} with id '{}'", messageArgs);
            return true;
        }
        if (withInterfaceNames) {
            bundleContext.registerService(getInterfacesNames(object), object, createProperties(getDatabaseType(), id));
        } else {
            bundleContext.registerService(object.getClass().getName(), object, createProperties(getDatabaseType(), id));
        }
        logger.info("OSGi service for {} successfully registered for DatabaseConnection of type {} with id '{}'", messageArgs);
        return true;
    }

    public static String createFilter(DatabaseTypes databaseType, String databaseId) {
        StringBuffer sb = new StringBuffer("(&(").append(DatabaseTypes.getKey()).append("=").append(databaseType.name())
                .append(")(").append(DATABASE_ID_KEY).append("=").append(databaseId).append("))");
        return sb.toString();
    }

    private Properties createProperties(DatabaseTypes databaseType, String databaseId) {
        Properties properties = new Properties();
        properties.put(DatabaseTypes.getKey(), databaseType.name());
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
