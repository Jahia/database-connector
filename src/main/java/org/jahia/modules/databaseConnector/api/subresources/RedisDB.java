/**
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 * <p>
 * http://www.jahia.com
 * <p>
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 * <p>
 * Copyright (C) 2002-2016 Jahia Solutions Group. All rights reserved.
 * <p>
 * This file is part of a Jahia's Enterprise Distribution.
 * <p>
 * Jahia's Enterprise Distributions must be used in accordance with the terms
 * contained in the Jahia Solutions Group Terms & Conditions as well as
 * the Jahia Sustainable Enterprise License (JSEL).
 * <p>
 * For questions regarding licensing, support, production usage...
 * please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 * <p>
 * ==========================================================================================
 */
package org.jahia.modules.databaseConnector.api.subresources;

import org.apache.commons.lang.StringUtils;
import org.jahia.modules.databaseConnector.api.impl.DatabaseConnector;
import org.jahia.modules.databaseConnector.connection.DatabaseConnectorManager;
import org.jahia.modules.databaseConnector.connection.DatabaseTypes;
import org.jahia.modules.databaseConnector.connection.redis.RedisConnection;
import org.jahia.services.content.JCRTemplate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * @author stefan on 2016-05-02.
 */
@Singleton
public class RedisDB {
    public static final String MAPPING = "redis";
    private static final Logger logger = LoggerFactory.getLogger(RedisDB.class);
    private DatabaseConnector databaseConnector;

    @Inject
    public RedisDB(JCRTemplate jcrTemplate, DatabaseConnectorManager databaseConnectorManager) {
        databaseConnector = new DatabaseConnector(databaseConnectorManager);
    }

    @GET
    @Path("/connection/{databaseId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConnection(@PathParam("databaseId") String databaseId) {
        return Response.status(Response.Status.OK).entity(databaseConnector.getConnection(databaseId, DatabaseTypes.REDIS)).build();
    }

    @GET
    @Path("/getconnections")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConnections() {
        try {
            return Response.status(Response.Status.OK).entity(databaseConnector.getConnections(DatabaseTypes.REDIS)).build();
        } catch (JSONException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Cannot parse json data\"}").build();
        }
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addConnection(String data) {
        try {
            JSONObject connectionParameters = new JSONObject(data);
            JSONArray missingParameters = new JSONArray();
            if (!connectionParameters.has("id") || StringUtils.isEmpty(connectionParameters.getString("id"))) {
                missingParameters.put("id");
            }
            if (!connectionParameters.has("host") || StringUtils.isEmpty(connectionParameters.getString("host"))) {
                missingParameters.put("host");
            }
            if (missingParameters.length() > 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"missingParameters\":" + missingParameters.toString() + "}").build();
            } else {
                String id = connectionParameters.has("id") ? connectionParameters.getString("id") : null;
                String host = connectionParameters.has("host") ? connectionParameters.getString("host") : null;
                Integer port = connectionParameters.has("port") && !StringUtils.isEmpty(connectionParameters.getString("port")) ? connectionParameters.getInt("port") : null;
                Boolean isConnected = connectionParameters.has("isConnected") && connectionParameters.getBoolean("isConnected");
                String dbName = connectionParameters.has("dbName") ? connectionParameters.getString("dbName") : null;
                String password = connectionParameters.has("password") ? connectionParameters.getString("password") : null;
                Long timeout = connectionParameters.has("timeout") && !StringUtils.isEmpty(connectionParameters.getString("timeout")) ? connectionParameters.getLong("timeout") : null;
                Integer weight = connectionParameters.has("weight") && !StringUtils.isEmpty(connectionParameters.getString("weight")) ? connectionParameters.getInt("weight") : null;
                String options = connectionParameters.has("options") ? connectionParameters.getString("options") : null;

                RedisConnection connection = new RedisConnection(id);

                connection.setHost(host);
                connection.setPort(port);
                connection.isConnected(isConnected);
                connection.setDbName(dbName);
                connection.setPassword(password);
                connection.setTimeout(timeout);
                connection.setWeight(weight);
                connection.setOptions(options);

                JSONObject jsonAnswer = new JSONObject();
                if (!databaseConnector.testConnection(connection)) {
                    connection.isConnected(false);
                    jsonAnswer.put("connectionVerified", false);
                } else {
                    jsonAnswer.put("connectionVerified", true);
                }
                databaseConnector.addEditConnection(connection, false);
                jsonAnswer.put("success", "Connection successfully added");
                logger.info("Successfully created RedisDB connection: " + id);
                return Response.status(Response.Status.OK).entity(jsonAnswer.toString()).build();
            }
        } catch (JSONException e) {
            logger.error("Cannot parse json data : {}", data);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Cannot parse json data\"}").build();
        }
    }


    @DELETE
    @Path("/remove/{connectionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeConnection(@PathParam("connectionId") String connectionId) {
        databaseConnector.removeConnection(connectionId, DatabaseTypes.REDIS);
        logger.info("Successfully deleted RedisDB connection: " + connectionId);
        return Response.status(Response.Status.OK).entity("{\"success\": \"Successfully removed RedisDB connection\"}").build();
    }

    @PUT
    @Path("/edit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editConnection(String data) {
        try {
            JSONObject connectionParameters = new JSONObject(data);
            JSONArray missingParameters = new JSONArray();
            if (!connectionParameters.has("id") || StringUtils.isEmpty(connectionParameters.getString("id"))) {
                missingParameters.put("id");
            }
            if (!connectionParameters.has("oldId") || StringUtils.isEmpty(connectionParameters.getString("oldId"))) {
                missingParameters.put("oldId");
            }
            if (!connectionParameters.has("host") || StringUtils.isEmpty(connectionParameters.getString("host"))) {
                missingParameters.put("host");
            }
            if (missingParameters.length() > 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"missingParameters\":" + missingParameters.toString() + "}").build();
            } else {
                String id = connectionParameters.has("id") ? connectionParameters.getString("id") : null;
                String oldId = connectionParameters.has("oldId") ? connectionParameters.getString("oldId") : null;
                String host = connectionParameters.has("host") ? connectionParameters.getString("host") : null;
                Integer port = connectionParameters.has("port") && !StringUtils.isEmpty(connectionParameters.getString("port")) ? connectionParameters.getInt("port") : null;
                Boolean isConnected = connectionParameters.has("isConnected") && connectionParameters.getBoolean("isConnected");
                String dbName = connectionParameters.has("dbName") ? connectionParameters.getString("dbName") : null;
                String password = connectionParameters.has("password") ? connectionParameters.getString("password") : null;
                Long timeout = connectionParameters.has("timeout") && !StringUtils.isEmpty(connectionParameters.getString("timeout")) ? connectionParameters.getLong("timeout") : null;
                Integer weight = connectionParameters.has("weight") && !StringUtils.isEmpty(connectionParameters.getString("weight")) ? connectionParameters.getInt("weight") : null;
                String options = connectionParameters.has("options") ? connectionParameters.getString("options") : null;

                RedisConnection connection = new RedisConnection(id);

                connection.setOldId(oldId);
                connection.setHost(host);
                connection.setPort(port);
                connection.isConnected(isConnected);
                connection.setDbName(dbName);
                connection.setPassword(password);
                connection.setTimeout(timeout);
                connection.setWeight(weight);
                connection.setOptions(options);

                JSONObject jsonAnswer = new JSONObject();
                if (!databaseConnector.testConnection(connection)) {
                    connection.isConnected(false);
                    jsonAnswer.put("connectionVerified", false);
                } else {
                    jsonAnswer.put("connectionVerified", true);
                }
                databaseConnector.addEditConnection(connection, true);
                jsonAnswer.put("success", "RedisDB Connection successfully edited");
                logger.info("Successfully edited RedisDB connection: " + id);
                return Response.status(Response.Status.OK).entity(jsonAnswer.toString()).build();
            }
        } catch (JSONException e) {
            logger.error("Cannot parse json data : {}", data);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Cannot parse json data\"}").build();
        }
    }

    @PUT
    @Path("/connect/{connectionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response connect(@PathParam("connectionId") String connectionId) {
        JSONObject jsonAnswer = new JSONObject();
        try {
            if (databaseConnector.updateConnection(connectionId, DatabaseTypes.REDIS, true)) {
                jsonAnswer.put("success", "Successfully connected to RedisDB");
                logger.info("Successfully enabled RedisDB connection, for connection with id: " + connectionId);
            } else {
                jsonAnswer.put("failed", "Connection failed to update");
                logger.info("Failed to establish RedisDB connection, for connection with id: " + connectionId);
            }
        } catch (JSONException ex) {
            logger.error(ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"\"Invalid connection parameter\"}").build();
        }
        return Response.status(Response.Status.OK).entity(jsonAnswer.toString()).build();
    }

    @PUT
    @Path("/disconnect/{connectionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response disconnect(@PathParam("connectionId") String connectionId) {
        databaseConnector.updateConnection(connectionId, DatabaseTypes.REDIS, false);
        logger.info("Successfully disconnected RedisDB connection, for connection with id: " + connectionId);
        return Response.status(Response.Status.OK).entity("{\"success\": \"Successfully disconnected from RedisDB\"}").build();
    }

    @GET
    @Path("/isconnectionvalid/{connectionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isConnectionIdAvailable(@PathParam("connectionId") String connectionId) {
        return Response.status(Response.Status.OK).entity(databaseConnector.isConnectionIdAvailable(connectionId, DatabaseTypes.REDIS)).build();
    }

    @POST
    @Path("/testconnection")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response testConnection(String data) {
        try {
            JSONObject connectionParameters = new JSONObject(data);
            JSONArray missingParameters = new JSONArray();
            if (!connectionParameters.has("id") || StringUtils.isEmpty(connectionParameters.getString("id"))) {
                missingParameters.put("id");
            }
            if (!connectionParameters.has("host") || StringUtils.isEmpty(connectionParameters.getString("host"))) {
                missingParameters.put("host");
            }
            if (missingParameters.length() > 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"missingParameters\":" + missingParameters.toString() + "}").build();
            } else {
                String id = connectionParameters.has("id") ? connectionParameters.getString("id") : null;
                String host = connectionParameters.has("host") ? connectionParameters.getString("host") : null;
                Integer port = connectionParameters.has("port") && !StringUtils.isEmpty(connectionParameters.getString("port")) ? connectionParameters.getInt("port") : null;
                Boolean isConnected = connectionParameters.has("isConnected") && connectionParameters.getBoolean("isConnected");
                String dbName = connectionParameters.has("dbName") ? connectionParameters.getString("dbName") : null;
                String password = connectionParameters.has("password") ? connectionParameters.getString("password") : null;
                Long timeout = connectionParameters.has("timeout") && !StringUtils.isEmpty(connectionParameters.getString("timeout")) ? connectionParameters.getLong("timeout") : null;
                Integer weight = connectionParameters.has("weight") && !StringUtils.isEmpty(connectionParameters.getString("weight")) ? connectionParameters.getInt("weight") : null;
                String options = connectionParameters.has("options") ? connectionParameters.getString("options") : null;

                RedisConnection connection = new RedisConnection(id);

                connection.setHost(host);
                connection.setPort(port);
                connection.isConnected(isConnected);
                connection.setDbName(dbName);
                connection.setPassword(password);
                connection.setTimeout(timeout);
                connection.setWeight(weight);
                connection.setOptions(options);

                boolean connectionTestPassed = databaseConnector.testConnection(connection);
                logger.info(connectionTestPassed ? "Connection test successfully passed" : "Connection test failed" + " for RedisDB with id: " + id);
                return Response.status(Response.Status.OK).entity("{\"result\": " + connectionTestPassed + "}").build();
            }
        } catch (JSONException e) {
            logger.error("Cannot parse json data : {}", data);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Cannot parse json data\"}").build();
        }
    }

    @GET
    @Path("/status/{connectionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServerStatus(@PathParam("connectionId") String connectionId) {
        try {
            Map<String, Object> serverStatus = databaseConnector.getServerStatus(connectionId, DatabaseTypes.REDIS);
            if (serverStatus.containsKey("failed")) {
                logger.info("Failed to retrieve Status for RedisDB connection with id: " + connectionId);
            } else {
                logger.info("Successfully retrieved Status for RedisDB connection with id: " + connectionId);
            }
            return Response.status(Response.Status.OK).entity(serverStatus).build();
        } catch (Exception e) {
            logger.error("Failed retrieve Status for RedisDB connection with id: " + connectionId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"failed\":\"Cannot get database status\"}").build();
        }
    }

}