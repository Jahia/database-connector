package org.jahia.modules.databaseConnector;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.jahia.modules.databaseConnector.DatabaseTypes.DATABASE_TYPE_KEY;

/**
 * Date: 11/1/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public abstract class AbstractDatabaseConnection implements DatabaseConnection, ConnectionDataFactory, Comparable<AbstractDatabaseConnection> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDatabaseConnection.class);

    protected final String id;

    public final static String ID_KEY = "dc:id";

    protected final String host;

    public final static String HOST_KEY = "dc:host";

    protected final Integer port;

    public final static String PORT_KEY = "dc:port";

    protected final String dbName;

    public final static String DB_NAME_KEY = "dc:dbName";

    protected final String uri;

    public final static String URI_KEY = "dc:uri";

    protected final String user;

    public final static String USER_KEY = "dc:user";

    protected final String password;

    public final static String PASSWORD_KEY = "dc:password";

    private final static String DATABASE_ID_KEY = "databaseId";

    private final List<ServiceRegistration> serviceRegistrations;

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
        this.serviceRegistrations = new ArrayList<ServiceRegistration>();
    }

    @Override
    public ConnectionData makeConnectionData() {
        return new ConnectionDataImpl(id, host, port, dbName, uri, user, password, getDatabaseType());
    }

    protected abstract boolean registerAsService();

    protected boolean registerAsService(Object object) {
        return registerAsService(object, false);
    }

    public void unregisterAsService() {
        logger.info("Start unregistering OSGi services for DatabaseConnection of type {} with id '{}'", getDatabaseType().getDisplayName(), id);
        for (ServiceRegistration serviceRegistration : serviceRegistrations) {
            serviceRegistration.unregister();
        }
        serviceRegistrations.clear();
        logger.info("OSGi services successfully unregistered for DatabaseConnection of type {} with id '{}'", getDatabaseType().getDisplayName(), id);
    }

    protected boolean registerAsService(Object object, boolean withInterfaceNames) {
        String[] messageArgs = {object.getClass().getSimpleName(), getDatabaseType().getDisplayName(), id};
        logger.info("Start registering OSGi service for {} for DatabaseConnection of type {} with id '{}'", messageArgs);
        ServiceReference[] serviceReferences;
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
        ServiceRegistration serviceRegistration;
        if (withInterfaceNames) {
            serviceRegistration = bundleContext.registerService(getInterfacesNames(object), object, createProperties(getDatabaseType(), id));
        } else {
            serviceRegistration = bundleContext.registerService(object.getClass().getName(), object, createProperties(getDatabaseType(), id));
        }
        serviceRegistrations.add(serviceRegistration);
        logger.info("OSGi service for {} successfully registered for DatabaseConnection of type {} with id '{}'", messageArgs);
        return true;
    }

    public static String createFilter(DatabaseTypes databaseType, String databaseId) {
        return "(&(" + DATABASE_TYPE_KEY + "=" + databaseType.name() + ")(" + DATABASE_ID_KEY + "=" + databaseId + "))";
    }

    public static String createSingleFilter(DatabaseTypes databaseType) {
        return "(" + DATABASE_TYPE_KEY + "=" + databaseType.name() + ")";
    }

    private Hashtable<String, String> createProperties(DatabaseTypes databaseType, String databaseId) {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(DATABASE_TYPE_KEY, databaseType.name());
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
