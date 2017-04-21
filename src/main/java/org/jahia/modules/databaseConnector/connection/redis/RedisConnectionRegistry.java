package org.jahia.modules.databaseConnector.connection.redis;

import org.apache.commons.lang.StringUtils;
import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.connection.AbstractDatabaseConnectionRegistry;
import org.jahia.modules.databaseConnector.services.DatabaseConnectorService;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.utils.EncryptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;
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
@Component
public class RedisConnectionRegistry extends AbstractDatabaseConnectionRegistry<RedisConnection> {

    private static final Logger logger = LoggerFactory.getLogger(RedisConnectionRegistry.class);
    private DatabaseConnectorService databaseConnectorService = null;
    private BundleContext context;

    public RedisConnectionRegistry() {
        super();
    }

    @Activate
    public void activate(BundleContext context) {
        this.context = context;
        this.databaseConnectorService.registerConnectorToRegistry(RedisConnection.DATABASE_TYPE, this);
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, service = DatabaseConnectorService.class)
    public void setDatabaseConnectorService(DatabaseConnectorService databaseConnectorService) {
        this.databaseConnectorService = databaseConnectorService;
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
    public void importConnection(Map<String, Object> map) {
        try {
            if (!ALPHA_NUMERIC_PATTERN.matcher((String)map.get("identifier")).matches()) {
                map.put("status", "failed");
                map.put("statusMessage", "invalidIdentifier");
                //Create instance to be able to parse the options of a failed connection.
                if (map.containsKey("options")) {
                    RedisConnection connection = new RedisConnection((String) map.get("identifier"));
                    map.put("options", map.containsKey("options") ? connection.parseOptions((LinkedHashMap) map.get("options")) : null);
                }
            } else if (databaseConnectorService.hasConnection((String) map.get("identifier"), (String) map.get("type"))) {
                map.put("status", "failed");
                map.put("statusMessage", "connectionExists");
                //Create instance to be able to parse the options of a failed connection.
                if (map.containsKey("options")) {
                    RedisConnection connection = new RedisConnection((String) map.get("identifier"));
                    map.put("options", map.containsKey("options") ? connection.parseOptions((LinkedHashMap) map.get("options")) : null);
                }
            } else {
                //Create connection object
                RedisConnection connection = new RedisConnection((String) map.get("identifier"));
                String host = map.containsKey("host") ? (String) map.get("host") : null;
                Integer port = map.containsKey("port") ? Integer.parseInt((String) map.get("port")) : RedisConnection.DEFAULT_PORT;
                Boolean isConnected = map.containsKey("isConnected") && Boolean.parseBoolean((String) map.get("isConnected"));
                String dbName = map.containsKey("dbName") ? (String) map.get("dbName") : RedisConnection.DEFAULT_DATABASE_NUMBER;
                String options = map.containsKey("options") ? connection.parseOptions((LinkedHashMap) map.get("options")) : null;
                map.put("options", options);
                String password = (String) map.get("password");
                Long timeout = map.containsKey("timeout") ? Long.parseLong((String) map.get("timeout")) : null;
                Integer weight = map.containsKey("weight") ? Integer.parseInt((String) map.get("weight")) : null;

                password = databaseConnectorService.setPassword(map, password);

                connection.setHost(host);
                connection.setPort(port);
                connection.isConnected(isConnected);
                connection.setDbName(dbName);
                connection.setPassword(password);
                connection.setWeight(weight);
                connection.setTimeout(timeout);
                connection.setOptions(options);

                databaseConnectorService.addEditConnection(connection, false);
                map.put("status", "success");
            }

        } catch (Exception ex) {
            map.put("status", "failed");
            map.put("statusMessage", "creationFailed");
            //try to parse options if the exist otherwise we will just remove them.
            try {
                if (map.containsKey("options")) {
                    RedisConnection connection = new RedisConnection((String) map.get("identifier"));
                    map.put("options", map.containsKey("options") ? connection.parseOptions((LinkedHashMap) map.get("options")) : null);
                }
            } catch (Exception e) {
                map.remove("options");
            }
            logger.info("Import " + (map.containsKey("identifier") ? "for connection: '" + map.get("identifier") + "'" : "") + " failed", ex.getMessage(), ex);
        }
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

