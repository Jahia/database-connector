/**
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group. All rights reserved.
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
package org.jahia.modules.databaseConnector.api.subresources;

import org.apache.commons.lang.StringUtils;
import org.jahia.modules.databaseConnector.connection.DatabaseConnectorManager;
import org.jahia.modules.databaseConnector.api.impl.DatabaseConnector;
import org.jahia.modules.databaseConnector.connection.DatabaseTypes;
import org.jahia.modules.databaseConnector.connection.mongo.MongoConnection;
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
import java.net.UnknownHostException;

/**
 * @author stefan on 2016-05-02.
 */
@Singleton
public class MongoDB {
    private static final Logger logger = LoggerFactory.getLogger(MongoDB.class);
    public static final String MAPPING = "mongodb";

    private DatabaseConnector databaseConnector;

    @Inject
    public MongoDB(JCRTemplate jcrTemplate, DatabaseConnectorManager databaseConnectorManager) {
        databaseConnector = new DatabaseConnector(jcrTemplate, databaseConnectorManager, logger);
    }

    //@TODO Remove when production ready
    @GET
    @Path("/test/{testParam}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJcrFormForPreview(@PathParam("testParam") String testParam) {
        return Response.status(Response.Status.OK).entity("{\"success\": \"We received your parameter: ' " + testParam.toString() + "'\"}").build();
    }

    @GET
    @Path("/connection/{databaseId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConnection(@PathParam("databaseId") String databaseId) {
        return Response.status(Response.Status.OK).entity(databaseConnector.getConnection(databaseId, DatabaseTypes.MONGO)).build();
    }

    @GET
    @Path("/getconnections")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConnections() {
        try {
            return Response.status(Response.Status.OK).entity(databaseConnector.getConnections(DatabaseTypes.MONGO)).build();
        } catch(JSONException e) {
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
            if (!connectionParameters.has("port") || StringUtils.isEmpty(connectionParameters.getString("port"))) {
                missingParameters.put("port");
            }
            if (!connectionParameters.has("dbName") || StringUtils.isEmpty(connectionParameters.getString("dbName"))) {
                missingParameters.put("dbName");
            }
            if (missingParameters.length() > 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"missingParameters\":" + missingParameters.toString() + "}").build();
            } else {
                String id = connectionParameters.has("id") ? connectionParameters.getString("id") : null;
                String host = connectionParameters.has("host") ? connectionParameters.getString("host") : null;
                Integer port = connectionParameters.has("port") ? connectionParameters.getInt("port") : null;
                Boolean isConnected = connectionParameters.has("isConnected") ? connectionParameters.getBoolean("isConnected") : false;
                String dbName = connectionParameters.has("dbName") ? connectionParameters.getString("dbName") : null;
                String user = connectionParameters.has("user") ? connectionParameters.getString("user") : null;
                String password = connectionParameters.has("password") ? connectionParameters.getString("password") : null;
                String writeConcern = connectionParameters.has("writeConcern") ? connectionParameters.getString("writeConcern") : null;
                String authDb = connectionParameters.has("authDb") ? connectionParameters.getString("authDb") : null;
                MongoConnection connection = new MongoConnection(id);
                connection.setHost(host);
                connection.setPort(port);
                connection.isConnected(isConnected);
                connection.setDbName(dbName);
                connection.setUser(user);
                connection.setPassword(password);
                connection.setWriteConcern(writeConcern);
                connection.setAuthDb(authDb);
                JSONObject jsonAnswer = new JSONObject();
                if (!databaseConnector.testConnection(connection)) {
                    connection.isConnected(false);
                    jsonAnswer.put("connectionVerified", false);
                } else {
                    jsonAnswer.put("connectionVerified", true);
                }
                databaseConnector.addEditConnection(connection, false);
                jsonAnswer.put("success", "Connection successfully added");
                logger.info("Successfully created MongoDB connection: " + id);
                return Response.status(Response.Status.OK).entity(jsonAnswer.toString()).build();
            }
        } catch(JSONException e) {
            logger.error("Cannot parse json data : {}", data);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Cannot parse json data\"}").build();
        }
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
            if (!connectionParameters.has("port") || StringUtils.isEmpty(connectionParameters.getString("port"))) {
                missingParameters.put("port");
            }
            if (!connectionParameters.has("dbName") || StringUtils.isEmpty(connectionParameters.getString("dbName"))) {
                missingParameters.put("dbName");
            }
            if (missingParameters.length() > 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"missingParameters\":" + missingParameters.toString() + "}").build();
            } else {
                String id = connectionParameters.has("id") ? connectionParameters.getString("id") : null;
                String oldId = connectionParameters.has("oldId") ? connectionParameters.getString("oldId") : null;
                String host = connectionParameters.has("host") ? connectionParameters.getString("host") : null;
                Integer port = connectionParameters.has("port") ? connectionParameters.getInt("port") : null;
                Boolean isConnected = connectionParameters.has("isConnected") ? connectionParameters.getBoolean("isConnected") : false;
                String dbName = connectionParameters.has("dbName") ? connectionParameters.getString("dbName") : null;
                String user = connectionParameters.has("user") ? connectionParameters.getString("user") : null;
                String password = connectionParameters.has("password") ? connectionParameters.getString("password") : null;
                String writeConcern = connectionParameters.has("writeConcern") ? connectionParameters.getString("writeConcern") : null;
                String authDb = connectionParameters.has("authDb") ? connectionParameters.getString("authDb") : null;
                MongoConnection connection = new MongoConnection(id);
                connection.setOldId(oldId);
                connection.setHost(host);
                connection.setPort(port);
                connection.isConnected(isConnected);
                connection.setDbName(dbName);
                connection.setUser(user);
                connection.setPassword(password);
                connection.setWriteConcern(writeConcern);
                connection.setAuthDb(authDb);
                JSONObject jsonAnswer = new JSONObject();
                if (!databaseConnector.testConnection(connection)) {
                    connection.isConnected(false);
                    jsonAnswer.put("connectionVerified", false);
                } else {
                    jsonAnswer.put("connectionVerified", true);
                }
                databaseConnector.addEditConnection(connection, true);
                jsonAnswer.put("success", "Connection successfully edited");
                logger.info("Successfully edited MongoDB connection: " + id);
                return Response.status(Response.Status.OK).entity(jsonAnswer.toString()).build();
            }
        } catch(JSONException e) {
            logger.error("Cannot parse json data : {}", data);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Cannot parse json data\"}").build();
        }
    }

    @DELETE
    @Path("/remove/{connectionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeConnection(@PathParam("connectionId") String connectionId) {
        databaseConnector.removeConnection(connectionId, DatabaseTypes.MONGO);
        logger.info("Successfully deleted MongoDB connection: " + connectionId);
        return Response.status(Response.Status.OK).entity("{\"success\": \"Successfully removed database connection\"}").build();
    }

    @PUT
    @Path("/connect/{connectionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response connect(@PathParam("connectionId") String connectionId) {
        JSONObject jsonAnswer = new JSONObject();
        try {
            if (databaseConnector.updateConnection(connectionId, DatabaseTypes.MONGO, true)) {
                jsonAnswer.put("success", "Successfully connected to database");
                logger.info("Successfully enabled MongoDB connection, for connection with id: " + connectionId);
            } else {
                jsonAnswer.put("failed", "Connection failed to update");
                logger.info("Failed to establish MongoDB connection, for connection with id: " + connectionId);
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
        databaseConnector.updateConnection(connectionId, DatabaseTypes.MONGO, false);
        logger.info("Successfully disconnected MongoDB connection, for connection with id: " + connectionId);
        return Response.status(Response.Status.OK).entity("{\"success\": \"Successfully disconnected from database\"}").build();
    }

    @GET
    @Path("/isconnectionvalid/{connectionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isConnectionIdAvailable(@PathParam("connectionId") String connectionId) {
        return Response.status(Response.Status.OK).entity(databaseConnector.isConnectionIdAvailable(connectionId, DatabaseTypes.MONGO)).build();
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
            if (!connectionParameters.has("port") || StringUtils.isEmpty(connectionParameters.getString("port"))) {
                missingParameters.put("port");
            }
            if (!connectionParameters.has("dbName") || StringUtils.isEmpty(connectionParameters.getString("dbName"))) {
                missingParameters.put("dbName");
            }
            if (missingParameters.length() > 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"missingParameters\":" + missingParameters.toString() + "}").build();
            } else {
                String id = connectionParameters.has("id") ? connectionParameters.getString("id") : null;
                String host = connectionParameters.has("host") ? connectionParameters.getString("host") : null;
                Integer port = connectionParameters.has("port") ? connectionParameters.getInt("port") : null;
                Boolean isConnected = connectionParameters.has("isConnected") ? connectionParameters.getBoolean("isConnected") : false;
                String dbName = connectionParameters.has("dbName") ? connectionParameters.getString("dbName") : null;
                String user = connectionParameters.has("user") ? connectionParameters.getString("user") : null;
                String password = connectionParameters.has("password") ? connectionParameters.getString("password") : null;
                String writeConcern = connectionParameters.has("writeConcern") ? connectionParameters.getString("writeConcern") : null;
                String authDb = connectionParameters.has("authDb") ? connectionParameters.getString("authDb") : null;
                MongoConnection connection = new MongoConnection(id);
                connection.setHost(host);
                connection.setPort(port);
                connection.isConnected(isConnected);
                connection.setDbName(dbName);
                connection.setUser(user);
                connection.setPassword(password);
                connection.setWriteConcern(writeConcern);
                connection.setAuthDb(authDb);
                boolean connectionTestPassed = databaseConnector.testConnection(connection);
                logger.info(connectionTestPassed ? "Connection test successfully passed" : "Connection test failed" + " for MongoDB with id: " + id);
                return Response.status(Response.Status.OK).entity("{\"result\": " + connectionTestPassed + "}").build();
            }
        } catch(JSONException e) {
            logger.error("Cannot parse json data : {}", data);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Cannot parse json data\"}").build();
        }
    }

}