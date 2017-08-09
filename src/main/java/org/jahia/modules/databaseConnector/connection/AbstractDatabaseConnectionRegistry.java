package org.jahia.modules.databaseConnector.connection;

import org.jahia.modules.databaseConnector.connector.ConnectorMetaData;
import org.jahia.modules.databaseConnector.services.ConnectionService;
import org.jahia.modules.databaseConnector.services.DatabaseConnectionRegistry;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.content.*;
import org.jahia.utils.EncryptionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.*;
import java.util.regex.Pattern;

import static org.jahia.modules.databaseConnector.util.Utils.query;
import static org.jahia.modules.databaseConnector.connection.AbstractConnection.*;
import static org.jahia.modules.databaseConnector.connection.DatabaseConnectorManager.*;

/**
 * Date: 11/6/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */

public abstract class AbstractDatabaseConnectionRegistry<T extends AbstractConnection> implements DatabaseConnectionRegistry<T> {

    private final static String NODE_TYPE = "dcmix:databaseConnection";

    private static final Logger logger = LoggerFactory.getLogger(AbstractDatabaseConnectionRegistry.class);

    protected Map<String, T> registry;

    protected JCRTemplate jcrTemplate;

    protected static final Pattern ALPHA_NUMERIC_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");

    protected ConnectorMetaData connectorMetaData;

    protected BundleContext context = null;

    private Map<String, ServiceRegistration> serviceRegistrations = new LinkedHashMap<>();

    private final static String DATABASE_ID_KEY = "databaseId";
    public final static String DATABASE_TYPE_KEY = "databaseType";

    public AbstractDatabaseConnectionRegistry() {
        this.jcrTemplate = JCRTemplate.getInstance();
        this.registry = new LinkedHashMap<>();
    }

    protected abstract boolean beforeAddEditConnection(final AbstractConnection connection, final boolean isEdition);

    public Map<String, T> getRegistry() {
        return registry;
    }

    protected Boolean storeConnection(final AbstractConnection connection, final String nodeType, final boolean isEdition) {

        JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {

            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper databaseConnectorNode = getDatabaseConnectorNode(session);
                session.checkout(databaseConnectorNode);
                JCRNodeWrapper connectionNode;
                if (isEdition) {
                    if (!connection.getId().equals(connection.getOldId()) && !isConnectionIdAvailable(connection.getId(), session)) {
                        return false;
                    }
                    connectionNode = (JCRNodeWrapper) getDatabaseConnectionNode(connection.getOldId(), session);
                    Assert.isTrue(connectionNode.getPrimaryNodeTypeName().equals(nodeType), "Stored node's primary type not equal " + nodeType);
                    session.checkout(connectionNode);

                } else {
                    // As is the id of a database connection is editable, if you need it to be final uncomment
                    // the next line and put the setProperty(ID_KEY, connection.getId()) inside the else statement.
                    // You will also need to add the attribute 'disabled="${isEdition}"' in the form input field
                    // for id in the file dc_serverSettings.html.serverSettings.flow.enterConfig.jsp and get rid of
                    // the javascript code corresponding to the warning modal
//                    connectionNode = databaseConnectorNode.addNode(connection.getId(), nodeType);
                    if (isConnectionIdAvailable(connection.getId(), session)) {
                        connectionNode = databaseConnectorNode.addNode(
                                JCRContentUtils.findAvailableNodeName(databaseConnectorNode, connection.getDatabaseType().toLowerCase()),
                                nodeType);
                    } else {
                        return false;
                    }
                }
                connectionNode.setProperty(ID_KEY, connection.getId());
                if (connection.getHost() != null) {
                    connectionNode.setProperty(HOST_KEY, connection.getHost());
                }
                if (connection.getPort() != null) {
                    connectionNode.setProperty(PORT_KEY, connection.getPort());
                }
                connectionNode.setProperty(IS_CONNECTED_KEY, connection.isConnected());
                if (connection.getUri() != null) {
                    connectionNode.setProperty(URI_KEY, connection.getUri());
                }
                if (connection.getDbName() != null) {
                    connectionNode.setProperty(DB_NAME_KEY, connection.getDbName());
                }
                if (connection.getUser() != null) {
                    connectionNode.setProperty(USER_KEY, connection.getUser());
                }
                if (connection.getPassword() != null) {
                    connectionNode.setProperty(PASSWORD_KEY, encodePassword(connection.getPassword()));
                }
                if (connection.getOptions() != null) {
                    connectionNode.setProperty(OPTIONS_KEY, connection.getOptions());
                }
                storeAdvancedConfig(connection, connectionNode);
                session.save();
                return true;
            }
        };
        try {
            return jcrTemplate.doExecuteWithSystemSession(callback);
        } catch (RepositoryException e) {
            return false;
        }
    }

    protected abstract void storeAdvancedConfig(AbstractConnection connection, JCRNodeWrapper node) throws RepositoryException;

    @Override
    public boolean addEditConnection (final AbstractConnection connection, final Boolean isEdition) {
        if (!beforeAddEditConnection(connection, isEdition)) {
            return false;
        }

        if (storeConnection(connection, connection.getNodeType(), isEdition)) {
            if (isEdition) {
                boolean wasConnected = connection.isConnected();
                if (registry.get(connection.getOldId()).isConnected()) {
                    unregisterAsService(connection);
                }
                if (!connection.getId().equals(connection.getOldId())) {
                    registry.remove(connection.getOldId());
                }
                if (wasConnected && connection.testConnectionCreation()) {
                    registerAsService(connection);
                } else {
                    connection.isConnected(false);
                }
                registry.put(connection.getId(), (T) connection);
            } else {

                registry.put(connection.getId(), (T) connection);
                if (connection.isConnected() && connection.testConnectionCreation()) {
                    registerAsService(connection);
                } else {
                    connection.isConnected(false);
                }

            }
            return true;

        } else {
            return false;
        }
    }

    public boolean removeConnection(final String databaseConnectionId) {
        Assert.isTrue(registry.containsKey(databaseConnectionId), "No database connection with ID: " + databaseConnectionId);
        if (registry.get(databaseConnectionId).isConnected()) {
            unregisterAsService(registry.get(databaseConnectionId));
        }
        JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {

            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                Node databaseConnectionNode = getDatabaseConnectionNode(databaseConnectionId, session);
                session.checkout(databaseConnectionNode);
                databaseConnectionNode.remove();
                session.save();
                return true;
            }
        };
        try {
            if (jcrTemplate.doExecuteWithSystemSession(callback)) {
                registry.remove(databaseConnectionId);
                return true;
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean connect(final String databaseConnectionId) {
        registerAsService(registry.get(databaseConnectionId));
        JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                Node databaseConnectionNode = getDatabaseConnectionNode(databaseConnectionId, session);
                session.checkout(databaseConnectionNode);
                databaseConnectionNode.setProperty(IS_CONNECTED_KEY, true);
                session.save();
                return true;
            }
        };
        try {
            if (jcrTemplate.doExecuteWithSystemSession(callback)) {
                return true;
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return true;
    }

    public boolean disconnect(final String databaseConnectionId) {
        unregisterAsService(registry.get(databaseConnectionId));
        JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                Node databaseConnectionNode = getDatabaseConnectionNode(databaseConnectionId, session);
                session.checkout(databaseConnectionNode);
                databaseConnectionNode.setProperty(IS_CONNECTED_KEY, false);
                session.save();
                return true;
            }
        };
        try {
            if (jcrTemplate.doExecuteWithSystemSession(callback)) {
                return true;
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return true;
    }

    private Node getDatabaseConnectionNode(String databaseConnectionId, JCRSessionWrapper session)
            throws RepositoryException, IllegalArgumentException {
        String statement = "SELECT * FROM [" + NODE_TYPE + "] WHERE [" + ID_KEY + "] = '" + databaseConnectionId + "'";
        NodeIterator nodes = query(statement, session).getNodes();
        if (!nodes.hasNext()) {
            throw new IllegalArgumentException("No database connection with ID '" + databaseConnectionId + "' stored in the JCR");
        }
        return nodes.nextNode();
    }

    private boolean isConnectionIdAvailable(String databaseConnectionId, JCRSessionWrapper session) throws RepositoryException {
        String statement = "SELECT * FROM [" + NODE_TYPE + "] WHERE [" + ID_KEY + "] = '" + databaseConnectionId + "'";
        NodeIterator nodes = query(statement, session).getNodes();
        return !nodes.hasNext();
    }

    protected JCRNodeWrapper getDatabaseConnectorNode(JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper settings = session.getNode(DATABASE_CONNECTOR_ROOT_PATH);
        if (settings.hasNode(DATABASE_CONNECTOR_PATH)) {
            return settings.getNode(DATABASE_CONNECTOR_PATH);
        } else {
            return settings.addNode(DATABASE_CONNECTOR_PATH, DATABASE_CONNECTOR_NODE_TYPE);
        }
    }

    private String encodePassword(String password) {
        return EncryptionUtils.passwordBaseEncrypt(password);
    }

    protected String decodePassword(JCRNodeWrapper connectionNode, String property) throws RepositoryException {
        return connectionNode.hasProperty(property) ? EncryptionUtils.passwordBaseDecrypt(connectionNode.getProperty(property).getString()) : null;
    }

    protected String setStringConnectionProperty(JCRNodeWrapper connectionNode, String property, boolean isMandatory) throws RepositoryException {
        return isMandatory ? connectionNode.getProperty(property).getString() : connectionNode.hasProperty(property) ? connectionNode.getProperty(property).getString() : null;
    }

    protected Boolean setBooleanConnectionProperty(JCRNodeWrapper connectionNode, String property) throws RepositoryException {
        return connectionNode.getProperty(property).getBoolean();
    }

    protected Integer setIntegerConnectionProperty(JCRNodeWrapper connectionNode, String property, boolean isMandatory) throws RepositoryException {
        if (connectionNode.hasProperty(property))
            return isMandatory ? (int) connectionNode.getProperty(property).getLong() : (int) connectionNode.getProperty(property).getLong();
        else
            return isMandatory ? (int) connectionNode.getProperty(property).getLong() : null;
    }

    protected Long setLongConnectionProperty(JCRNodeWrapper connectionNode, String property, boolean isMandatory) throws RepositoryException {
        if (connectionNode.hasProperty(property))
            return isMandatory ? connectionNode.getProperty(property).getLong() : connectionNode.getProperty(property).getLong();
        else
            return isMandatory ? connectionNode.getProperty(property).getLong() : null;
    }

    public boolean testConnection(AbstractConnection connection) {
        return connection.testConnectionCreation();
    }

    public void buildConnectionMapFromJSON(Map<String, Object> result, JSONObject jsonConnectionData) throws JSONException {
        prepareConnectionMapFromJSON(result, jsonConnectionData);
    }

    public Map<String, Object> buildConnectionMapFromConnection(AbstractConnection connection) throws JSONException {
        return prepareConnectionMapFromConnection(connection);
    }

    public AbstractConnection getConnection(String connectionId) {
        return registry.get(connectionId);
    }

    public void closeConnections() {
        for (Map.Entry<String, T> entry : registry.entrySet()) {
            unregisterAsService(entry.getValue());
        }
    };

    public void setConnectorProperties(String moduleName, String registryClassName) {
        this.connectorMetaData = new ConnectorMetaData(
                getConnectionType(),
                getConnectionDisplayName(),
                getEntryPoint(),
                moduleName,
                registryClassName
        );
    }

    @Override
    public ConnectorMetaData getConnectorMetaData() {
        return this.connectorMetaData;
    }

    protected void registerAsService(AbstractConnection connection) {
        Object service = connection.beforeRegisterAsService();
        registerAsService(service, connection);
        connection.isConnected(true);
    }

    private boolean registerAsService(Object object, AbstractConnection connection) {
        String[] messageArgs = {object.getClass().getSimpleName(), connection.getDisplayName(), connection.getId()};
        logger.info("Start registering OSGi service for {} for DatabaseConnection of type {} with id '{}'", messageArgs);
        ServiceReference[] serviceReferences;
        try {
            serviceReferences = this.context.getAllServiceReferences(ConnectionService.class.getName(), createFilter(connection.getDatabaseType(), connection.getId()));
        } catch (InvalidSyntaxException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        if (serviceReferences != null) {
            logger.info("OSGi service for {} already registered for DatabaseConnection of type {} with id '{}'", messageArgs);
            return true;
        }
        ServiceRegistration serviceRegistration;
        serviceRegistration = this.context.registerService(getInterfacesNames(object), object, createProperties(connection.getDatabaseType(), connection.getId()));
        this.serviceRegistrations.put(connection.getId(), serviceRegistration);
        logger.info("OSGi service for {} successfully registered for DatabaseConnection of type {} with id '{}'", messageArgs);
        return true;
    }

    protected void unregisterAsService(AbstractConnection connection) {
        connection.beforeUnregisterAsService();
        logger.info("Start unregistering OSGi services for DatabaseConnection of type {} with id '{}'", connection.getDisplayName(), connection.getId());
        ServiceRegistration serviceRegistration = this.serviceRegistrations.get(connection.getId());
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            this.serviceRegistrations.remove(connection.getId());
        }
        connection.isConnected = false;
        logger.info("OSGi services successfully unregistered for DatabaseConnection of type {} with id '{}'", connection.getDisplayName(), connection.getId());
    }

    private String[] getInterfacesNames(Object obj) {
        Class[] interfaces = obj.getClass().getInterfaces();
        String[] interfacesNames = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            interfacesNames[i] = interfaces[i].getName();
        }
        return interfacesNames;
    }

    private Hashtable<String, String> createProperties(String databaseType, String databaseId) {
        Hashtable<String, String> properties = new Hashtable<>();
        properties.put(DATABASE_TYPE_KEY, databaseType);
        properties.put(DATABASE_ID_KEY, databaseId);
        return properties;
    }

    public static String createFilter(String databaseType, String databaseId) {
        return "(&(" + DATABASE_TYPE_KEY + "=" + databaseType + ")(" + DATABASE_ID_KEY + "=" + databaseId + "))";
    }

    public void registerServices() {
        for (Map.Entry<String, T> entry: registry.entrySet()) {
            AbstractConnection connection = entry.getValue();
            if (connection.isConnected()) {
                registerAsService(connection);
            }
        }
    }

    public Object getConnectionService(String databseType, String connectionId) {
        return getConnectionService(ConnectionService.class, databseType, connectionId) ;
    }

    public Object getConnectionService(Class c, String databseType, String connectionId) {
       return BundleUtils.getOsgiService(c, createFilter(databseType, connectionId)) ;
    }
}

