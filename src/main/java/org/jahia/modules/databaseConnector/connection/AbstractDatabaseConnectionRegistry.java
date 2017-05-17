package org.jahia.modules.databaseConnector.connection;

import org.jahia.modules.databaseConnector.connector.AbstractConnectorMetaData;
import org.jahia.services.content.*;
import org.jahia.utils.EncryptionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
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

public abstract class AbstractDatabaseConnectionRegistry<T> implements DatabaseConnectionRegistry<T> {

    private final static String NODE_TYPE = "dcmix:databaseConnection";

    private static final Logger logger = LoggerFactory.getLogger(AbstractDatabaseConnectionRegistry.class);

    protected Map<String, T> registry;

    protected JCRTemplate jcrTemplate;

    protected static final Pattern ALPHA_NUMERIC_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");

    protected AbstractConnectorMetaData connectorMetaData;

    protected String databseType = null;

    protected String displayName = null;

    public AbstractDatabaseConnectionRegistry() {
        this.jcrTemplate = JCRTemplate.getInstance();
        this.registry = new LinkedHashMap<>();
    }

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

    protected void storeAdvancedConfig(AbstractConnection connection, JCRNodeWrapper node) throws RepositoryException {
    }

    public boolean removeConnection(final String databaseConnectionId) {
        Assert.isTrue(registry.containsKey(databaseConnectionId), "No database connection with ID: " + databaseConnectionId);
        if (((AbstractConnection) registry.get(databaseConnectionId)).isConnected()) {
            ((AbstractConnection) registry.get(databaseConnectionId)).forgetConnection();
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
        ((AbstractConnection) registry.get(databaseConnectionId)).establishConnection();
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
        ((AbstractConnection) registry.get(databaseConnectionId)).forgetConnection();
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
        return (AbstractConnection) registry.get(connectionId);
    }

    public String getDatabseType() {
        return databseType;
    }

    public void setDatabseType(String databseType) {
        this.databseType = databseType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Object getClient(String connectionId) {
        return ((AbstractConnection)registry.get(connectionId)).getClient(connectionId);
    }

    public abstract Object getDatabase(String connectionId);

    public void closeConnections() {
        for (Map.Entry<String, T> entry : registry.entrySet()) {
            ((AbstractConnection)entry.getValue()).forgetConnection();
        }
    };
}

