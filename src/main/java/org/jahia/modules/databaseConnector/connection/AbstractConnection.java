package org.jahia.modules.databaseConnector.connection;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import static org.jahia.modules.databaseConnector.connection.DatabaseTypes.DATABASE_TYPE_KEY;

/**
 * @author stefan on 2016-05-10.
 */

public abstract class AbstractConnection <T extends ConnectionData> implements Serializable {

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

    protected Boolean isConnected;

    protected DatabaseTypes databaseType;

    public final static String ID_KEY = "dc:id";

    public final static String HOST_KEY = "dc:host";

    public final static String PORT_KEY = "dc:port";

    public final static String DB_NAME_KEY = "dc:dbName";

    public final static String URI_KEY = "dc:uri";

    public final static String USER_KEY = "dc:user";

    public final static String PASSWORD_KEY = "dc:password";

    public final static String IS_CONNECTED_KEY = "dc:isConnected";

    private final static String DATABASE_ID_KEY = "databaseId";

    private final List<ServiceRegistration> serviceRegistrations = new LinkedList<>();

    protected abstract Object beforeRegisterAsService();

    public abstract void beforeUnregisterAsService();

    public void registerAsService() {
        Object service = beforeRegisterAsService();
        registerAsService(service, false);
        this.isConnected = true;
    }

    public void unregisterAsService() {
        beforeUnregisterAsService();
        logger.info("Start unregistering OSGi services for DatabaseConnection of type {} with id '{}'", getDatabaseType().getDisplayName(), id);
        for (ServiceRegistration serviceRegistration : serviceRegistrations) {
            serviceRegistration.unregister();
        }
        serviceRegistrations.clear();
        this.isConnected = false;
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

    public DatabaseTypes getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseTypes databaseType) {
        this.databaseType = databaseType;
    }

    public String getDisplayName() {
        return databaseType.getDisplayName();
    }

    public abstract T makeConnectionData();

}

