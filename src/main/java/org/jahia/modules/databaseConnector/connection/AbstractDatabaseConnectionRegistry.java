/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms & Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.modules.databaseConnector.connection;

import org.apache.commons.lang3.ClassUtils;
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
    private static final Logger logger = LoggerFactory.getLogger(AbstractDatabaseConnectionRegistry.class);

    public final static String DATABASE_ID_KEY = "databaseId";
    public final static String DATABASE_TYPE_KEY = "databaseType";
    public final static String DATABASE_CONNECTION_PATH_PROPERTY = "connectionPath";

    protected Map<String, T> registry;

    protected JCRTemplate jcrTemplate;

    protected ConnectorMetaData connectorMetaData;

    protected BundleContext context = null;

    private Map<String, ServiceRegistration> serviceRegistrations = new LinkedHashMap<>();

    public AbstractDatabaseConnectionRegistry() {
        this.jcrTemplate = JCRTemplate.getInstance();
        this.registry = new LinkedHashMap<>();
    }

    protected abstract boolean beforeAddEditConnection(final AbstractConnection connection, final boolean isEdition);

    public Map<String, T> getRegistry() {
        return registry;
    }

    public Map<String, ServiceRegistration> getRegistrations() { return this.serviceRegistrations; }

    protected Boolean storeConnection(final AbstractConnection connection, final String nodeType, final boolean isEdition) {

        JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {

            //TODO store connection for every connection as property
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper databaseConnectorNode = getDatabaseConnectorNode(session);
                String dbID = JCRContentUtils.generateNodeName(connection.getId());
                session.checkout(databaseConnectorNode);
                JCRNodeWrapper connectionNode;
                if (isEdition) {
                    if (!connection.getId().equals(connection.getOldId()) && !databaseConnectorNode.hasNode(dbID)) {
                        return false;
                    }
                    connectionNode = (JCRNodeWrapper) getDatabaseConnectionNode(connection.getOldId(), session);
                    Assert.isTrue(connectionNode.getPrimaryNodeTypeName().equals(nodeType), "Stored node's primary type not equal " + nodeType);
                    session.checkout(connectionNode);

                } else {
                    // As is the id of a database connection is editable, if you need it to be final uncomment
                    // the next line and put the setProperty(ID_PROPERTY, connection.getId()) inside the else statement.
                    // You will also need to add the attribute 'disabled="${isEdition}"' in the form input field
                    // for id in the file dc_serverSettings.html.serverSettings.flow.enterConfig.jsp and get rid of
                    // the javascript code corresponding to the warning modal
//                    connectionNode = databaseConnectorNode.addNode(connection.getId(), nodeType);
                    if (databaseConnectorNode.hasNode(dbID)) {
                        return false;
                    }
                    connectionNode = databaseConnectorNode.addNode(dbID, nodeType);
                }
                connectionNode.setProperty(ID_PROPERTY, connection.getId());

                if (connection.getDatabaseType() != null) {
                    connectionNode.setProperty(DATABASE_TYPE_PROPETRY, connection.getDatabaseType());
                }
                if (connection.getHost() != null) {
                    connectionNode.setProperty(HOST_PROPERTY, connection.getHost());
                }
                if (connection.getPort() != null) {
                    connectionNode.setProperty(PORT_PROPERTY, connection.getPort());
                }
                connectionNode.setProperty(IS_CONNECTED_PROPERTY, connection.isConnected());
                if (connection.getUri() != null) {
                    connectionNode.setProperty(URI_PROPERTY, connection.getUri());
                }
                if (connection.getDbName() != null) {
                    connectionNode.setProperty(DB_NAME_PROPERTY, connection.getDbName());
                }
                if (connection.getUser() != null) {
                    connectionNode.setProperty(USER_PROPERTY, connection.getUser());
                }
                if (connection.getPassword() != null) {
                    connectionNode.setProperty(PASSWORD_PROPERTY, encodePassword(connection.getPassword()));
                }
                if (connection.getOptions() != null) {
                    connectionNode.setProperty(OPTIONS_PROPERTY, connection.getOptions());
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
            return addEditConnectionNoStore(connection, isEdition);
        } else {
            return false;
        }
    }

    @Override
    public boolean addEditConnectionNoStore (final AbstractConnection connection, final Boolean isEdition) {
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

    public void unregisterAndRemoveFromRegistry(String databaseConnectionId) {
        //Same as above except we don't clear JCR, this one is needed for cluster processing
        Assert.isTrue(registry.containsKey(databaseConnectionId), "No database connection with ID: " + databaseConnectionId);
        if (registry.get(databaseConnectionId).isConnected()) {
            unregisterAsService(registry.get(databaseConnectionId));
        }
        registry.remove(databaseConnectionId);
    }

    public boolean connect(final String databaseConnectionId) {
        registerAsService(registry.get(databaseConnectionId));
        JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                Node databaseConnectionNode = getDatabaseConnectionNode(databaseConnectionId, session);
                session.checkout(databaseConnectionNode);
                databaseConnectionNode.setProperty(IS_CONNECTED_PROPERTY, true);
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
                databaseConnectionNode.setProperty(IS_CONNECTED_PROPERTY, false);
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
        String statement = "SELECT * FROM [" + getConnectionNodeType() + "] WHERE [" + ID_PROPERTY + "] = '" + databaseConnectionId + "'";
        NodeIterator nodes = query(statement, session).getNodes();
        if (!nodes.hasNext()) {
            throw new IllegalArgumentException("No database connection with ID '" + databaseConnectionId + "' stored in the JCR");
        }
        return nodes.nextNode();
    }

    //TODO why do we do that???
    private boolean isConnectionIdAvailable(String databaseConnectionId, JCRSessionWrapper session) throws RepositoryException {
        String statement = "SELECT * FROM [" + getConnectionNodeType() + "] WHERE [" + ID_PROPERTY + "] = '" + databaseConnectionId + "'";
        NodeIterator nodes = query(statement, session).getNodes();
        return !nodes.hasNext();
    }

    protected JCRNodeWrapper getDatabaseConnectorNode(JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper settings = session.getNode(DATABASE_CONNECTOR_ROOT_PATH);
        if (settings.hasNode(DATABASE_CONNECTOR_PATH)) {
            return settings.getNode(DATABASE_CONNECTOR_PATH);
        } else {
            JCRNodeWrapper dbConnectorNode = settings.addNode(DATABASE_CONNECTOR_PATH, DATABASE_CONNECTOR_NODE_TYPE);
            dbConnectorNode.setAclInheritanceBreak(true);
            return dbConnectorNode;
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
        serviceRegistration = this.context.registerService(getInterfacesNames(object), object, createProperties(connection.getDatabaseType(), connection.getId(), connection.getPath()));
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
        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(obj.getClass());
        List<String> interfacesNames = new ArrayList<>();
        for (Class<?> interfaceClass : interfaces) {
            interfacesNames.add(interfaceClass.getName());
        }
        return interfacesNames.toArray(new String[0]);
    }

    private Hashtable<String, String> createProperties(String databaseType, String databaseId, String connectionPath) {
        Hashtable<String, String> properties = new Hashtable<>();
        properties.put(DATABASE_TYPE_KEY, databaseType);
        properties.put(DATABASE_ID_KEY, databaseId);
        properties.put(DATABASE_CONNECTION_PATH_PROPERTY, connectionPath);
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

    public Object getConnectionService(String databaseType, String connectionId) {
        return getConnectionService(ConnectionService.class, databaseType, connectionId) ;
    }

    public Object getConnectionService(Class c, String databaseType, String connectionId) {
       return BundleUtils.getOsgiService(c, createFilter(databaseType, connectionId)) ;
    }

    public List<Map<String, Object>> getConnectionsInfo(String databaseType) {
        List<Map<String, Object>> connectionsInfoList = new LinkedList<>();
        for (Map.Entry<String, T> connection : registry.entrySet()) {
            AbstractConnection abstractConnection = connection.getValue();
            ConnectionService cs = BundleUtils.getOsgiService(ConnectionService.class, createFilter(databaseType, connection.getValue().getId()));
            if (cs != null) {
                Map<String, Object> connectionInfo = new LinkedHashMap<>();
                connectionInfo.put("id", abstractConnection.getId());
                connectionInfo.put("displayName", abstractConnection.getDisplayName());
                connectionInfo.put("databaseType", abstractConnection.getDatabaseType());
                connectionInfo.put("host", abstractConnection.getHost());
                connectionInfo.put("port", abstractConnection.getPort());
                connectionsInfoList.add(connectionInfo);
            }
        }
        return connectionsInfoList;
    }

    protected abstract String getConnectionNodeType();
}

