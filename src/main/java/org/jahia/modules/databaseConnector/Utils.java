package org.jahia.modules.databaseConnector;

import org.apache.commons.lang.StringUtils;
import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.connection.DatabaseTypes;
import org.jahia.modules.databaseConnector.connection.mongo.MongoConnection;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.utils.EncryptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Date: 12/3/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class Utils {
    public final static String NEW_LINE = System.getProperty("line.separator");//This will retrieve line separator dependent on OS.
    public final static char TABU = '\u0009';
    public final static char DOUBLE_QUOTE = '\u0022';

    public static QueryResult query(String statement, JCRSessionWrapper session) throws RepositoryException {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query query = queryManager.createQuery(statement, Query.JCR_SQL2);
        return query.execute();
    }

    public static Map<String, Object> buildConnection(JSONObject jsonConnectionData) throws JSONException {
        Map<String, Object> result = new LinkedHashMap<>();
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
        if (!jsonConnectionData.has("dbName") || StringUtils.isEmpty(jsonConnectionData.getString("dbName"))) {
            missingParameters.put("dbName");
        }
        if (missingParameters.length() > 0) {
            result.put("connectionStatus", "failed");
        } else {
            String id = jsonConnectionData.has("id") ? jsonConnectionData.getString("id") : null;
            String host = jsonConnectionData.has("host") ? jsonConnectionData.getString("host") : null;
            Integer port = jsonConnectionData.has("port") && !StringUtils.isEmpty(jsonConnectionData.getString("port")) ? jsonConnectionData.getInt("port") : null;
            Boolean isConnected = jsonConnectionData.has("isConnected") ? jsonConnectionData.getBoolean("isConnected") : false;
            String dbName = jsonConnectionData.has("dbName") ? jsonConnectionData.getString("dbName") : null;
            String user = jsonConnectionData.has("user") ? jsonConnectionData.getString("user") : null;
            String password = jsonConnectionData.has("password") ? jsonConnectionData.getString("password") : null;
            AbstractConnection connection = null;
            switch (DatabaseTypes.valueOf((String)jsonConnectionData.get("databaseType"))) {
                case MONGO:
                    connection = new MongoConnection(id);
                    String writeConcern = jsonConnectionData.has("writeConcern") ? jsonConnectionData.getString("writeConcern") : null;
                    String authDb = jsonConnectionData.has("authDb") ? jsonConnectionData.getString("authDb") : null;
                    ((MongoConnection)connection).setWriteConcern(writeConcern);
                    ((MongoConnection)connection).setAuthDb(authDb);
                    break;
                case REDIS:
                    break;
            }
            connection.setHost(host);
            connection.setPort(port);
            connection.isConnected(isConnected);
            connection.setDbName(dbName);
            connection.setUser(user);
            if(password != null && password.contains("_ENC")) {
                password = password.substring(0,32);
                password = EncryptionUtils.passwordBaseDecrypt(password);
            }
            connection.setPassword(password);

            result.put("connectionStatus", "success");
            result.put("connection", connection);
        }
        return result;
    }

    public static Map<String, Object> buildConnectionMap(AbstractConnection connection) throws JSONException{
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", connection.getId());
        result.put("host", connection.getHost());
        result.put("isConnected", connection.isConnected());
        result.put("dbName", connection.getDbName());
        result.put("databaseType", connection.getDatabaseType());
        result.put("user", connection.getUser());
        if (!StringUtils.isEmpty(connection.getPassword())) {
            result.put("password", EncryptionUtils.passwordBaseEncrypt(connection.getPassword()) + "_ENC");
        }
        switch (connection.getDatabaseType()) {
            case MONGO:
                result.put("authDb", ((MongoConnection)connection).getAuthDb());
                result.put("writeConcern", ((MongoConnection)connection).getWriteConcern());
                break;
            case REDIS:
                break;
        }
        return result;
    }
}
