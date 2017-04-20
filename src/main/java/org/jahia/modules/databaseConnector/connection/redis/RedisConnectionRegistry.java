package org.jahia.modules.databaseConnector.connection.redis;

import org.apache.commons.lang.StringUtils;
import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.connection.AbstractDatabaseConnectionRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.utils.EncryptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jahia.modules.databaseConnector.util.Utils.query;
import static org.jahia.modules.databaseConnector.connection.AbstractConnection.DB_NAME_KEY;
import static org.jahia.modules.databaseConnector.connection.AbstractConnection.HOST_KEY;
import static org.jahia.modules.databaseConnector.connection.AbstractConnection.ID_KEY;
import static org.jahia.modules.databaseConnector.connection.AbstractConnection.IS_CONNECTED_KEY;
import static org.jahia.modules.databaseConnector.connection.AbstractConnection.PASSWORD_KEY;
import static org.jahia.modules.databaseConnector.connection.AbstractConnection.PORT_KEY;
import static org.jahia.modules.databaseConnector.connection.redis.RedisConnection.*;
import static org.jahia.modules.databaseConnector.connection.redis.RedisConnection.OPTIONS_KEY;


/**
 * Date: 11/6/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class RedisConnectionRegistry extends AbstractDatabaseConnectionRegistry<RedisConnection> {

    private static final Logger logger = LoggerFactory.getLogger(RedisConnectionRegistry.class);

    public RedisConnectionRegistry() {
        super();
    }

    @Override
    public Map<String, RedisConnection> populateRegistry() {
        JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {

            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                QueryResult queryResult = query("SELECT * FROM [" + NODE_TYPE + "]", session);
                NodeIterator it = queryResult.getNodes();
                while (it.hasNext()) {
                    JCRNodeWrapper connectionNode = (JCRNodeWrapper) it.next();
                    String id = setStringConnectionProperty(connectionNode, ID_KEY, true);
                    String host = setStringConnectionProperty(connectionNode, HOST_KEY, true);
                    Integer port = setIntegerConnectionProperty(connectionNode, PORT_KEY, true);
                    Boolean isConnected = setBooleanConnectionProperty(connectionNode, IS_CONNECTED_KEY);
                    String dbName = setStringConnectionProperty(connectionNode, DB_NAME_KEY, false);
                    String password = decodePassword(connectionNode, PASSWORD_KEY);
                    Long timeout = setLongConnectionProperty(connectionNode, TIMEOUT_KEY, false);
                    Integer weight = setIntegerConnectionProperty(connectionNode, WEIGHT_KEY, false);
                    String options = setStringConnectionProperty(connectionNode, OPTIONS_KEY, false);
                    RedisConnection storedConnection = new RedisConnection(id);
                    storedConnection.setOldId(id);
                    storedConnection.setHost(host);
                    storedConnection.setPort(port);
                    storedConnection.isConnected(isConnected);
                    storedConnection.setDbName(dbName);
                    storedConnection.setPassword(password);
                    storedConnection.setTimeout(timeout);
                    storedConnection.setWeight(weight);
                    storedConnection.setOptions(options);
                    registry.put(id, storedConnection);
                }
                return true;
            }
        };
        try {
            jcrTemplate.doExecuteWithSystemSession(callback);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return registry;
    }

    @Override
    public boolean addEditConnection(final AbstractConnection connection, final Boolean isEdition) {
        Assert.hasText(connection.getHost(), "Host must be defined");

        RedisConnection redisConnection = (RedisConnection) connection;
        if (storeConnection(redisConnection, NODE_TYPE, isEdition)) {
            if (isEdition) {
                if (registry.get(redisConnection.getOldId()).isConnected()) {
                    registry.get(connection.getOldId()).unregisterAsService();
                }
                if (!redisConnection.getId().equals(connection.getOldId())) {
                    registry.remove(connection.getOldId());
                }
                if (redisConnection.isConnected() && redisConnection.testConnectionCreation()) {
                    redisConnection.registerAsService();
                } else {
                    redisConnection.isConnected(false);
                }
                registry.put(redisConnection.getId(), redisConnection);

            } else {

                registry.put(redisConnection.getId(), redisConnection);
                if (redisConnection.isConnected() && redisConnection.testConnectionCreation()) {
                    redisConnection.registerAsService();
                } else {
                    redisConnection.isConnected(false);
                }

            }
            return true;

        } else {
            return false;
        }
    }


    @Override
    public boolean importConnection(Map<String, Object> map) {
        return false;
    }

    @Override
    protected void storeAdvancedConfig(AbstractConnection connection, JCRNodeWrapper node) throws RepositoryException {
        RedisConnection redisConnection = (RedisConnection) connection;
        if (redisConnection.getTimeout() != null) {
            node.setProperty(TIMEOUT_KEY, redisConnection.getTimeout());
        }
        if (redisConnection.getWeight() != null) {
            node.setProperty(WEIGHT_KEY, redisConnection.getWeight());
        }
    }

    @Override
    public String getConnectionType() {
        return RedisConnection.DATABASE_TYPE;
    }

    @Override
    public String getConnectionDisplayName() {
        return null;
    }

    @Override
    public Map<String, Object> prepareConnectionMapFromJSON(Map<String, Object> result, JSONObject jsonConnectionData) throws JSONException {
        JSONArray missingParameters = new JSONArray();
        if (jsonConnectionData.has("reImport")) {
            result.put("reImport", jsonConnectionData.getString("reImport"));
        }
        if (!jsonConnectionData.has("id") || StringUtils.isEmpty(jsonConnectionData.getString("id"))) {
            missingParameters.put("id");
        }
        if (!jsonConnectionData.has("host") || StringUtils.isEmpty(jsonConnectionData.getString("host"))) {
            missingParameters.put("host");
        }
        if ((!jsonConnectionData.has("dbName") || StringUtils.isEmpty(jsonConnectionData.getString("dbName")))) {
            missingParameters.put("dbName");
        }
        if (missingParameters.length() > 0) {
            result.put("connectionStatus", "failed");
        } else {
            String id = jsonConnectionData.getString("id");
            String host = jsonConnectionData.getString("host");
            Integer port = jsonConnectionData.has("port") && !StringUtils.isEmpty(jsonConnectionData.getString("port")) ? jsonConnectionData.getInt("port") : null;
            Boolean isConnected = jsonConnectionData.has("isConnected") && jsonConnectionData.getBoolean("isConnected");
            String dbName = jsonConnectionData.has("dbName") ? jsonConnectionData.getString("dbName") : null;
            String password = jsonConnectionData.has("password") ? jsonConnectionData.getString("password") : null;
            String options = jsonConnectionData.has("options") ? jsonConnectionData.getString("options") : null;

            RedisConnection connection = new RedisConnection(id);
            if (jsonConnectionData.has("timeout") && !StringUtils.isEmpty(jsonConnectionData.getString("timeout"))) {
                connection.setTimeout(jsonConnectionData.getLong("timeout"));
            }
            if (jsonConnectionData.has("weight") && !StringUtils.isEmpty(jsonConnectionData.getString("weight"))) {
                connection.setWeight(jsonConnectionData.getInt("weight"));
            }

            connection.setHost(host);
            connection.setPort(port);
            connection.isConnected(isConnected);
            connection.setDbName(dbName);
            if (password != null && password.contains("_ENC")) {
                password = password.substring(0, 32);
                password = EncryptionUtils.passwordBaseDecrypt(password);
            }
            connection.setPassword(password);
            connection.setOptions(options);
            result.put("connectionStatus", "success");
            result.put("connection", connection);
        }
        return result;
    }

    @Override
    public Map<String, Object> prepareConnectionMapFromConnection(AbstractConnection connection) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", connection.getId());
        result.put("host", connection.getHost());
        result.put("isConnected", connection.isConnected());
        result.put("dbName", connection.getDbName());
        result.put("databaseType", connection.getDatabaseType());
        result.put("options", connection.getOptions());
        if (!StringUtils.isEmpty(connection.getPassword())) {
            result.put("password", EncryptionUtils.passwordBaseEncrypt(connection.getPassword()) + "_ENC");

        }
        result.put("timeout", ((RedisConnection) connection).getTimeout());
        result.put("weight", ((RedisConnection) connection).getWeight());
        return  result;
    }
}

