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
    public static final String MAPPING = "redisdb";
    private static final Logger logger = LoggerFactory.getLogger(RedisDB.class);
    private DatabaseConnector databaseConnector;

    @Inject
    public RedisDB(JCRTemplate jcrTemplate, DatabaseConnectorManager databaseConnectorManager) {
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
        return Response.status(Response.Status.OK).entity(databaseConnector.getConnection(databaseId, DatabaseTypes.REDIS)).build();
    }

    @GET
    @Path("/getconnections")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConnections() {
        try {
            return Response.status(Response.Status.OK).entity(databaseConnector.getConnections(DatabaseTypes.REDIS)).build();
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
            if (!connectionParameters.has("dbName") || StringUtils.isEmpty(connectionParameters.getString("dbName"))) {
                missingParameters.put("dbName");
            }
            if (missingParameters.length() > 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"missingParameters\":" + missingParameters.toString() + "}").build();
            } else {
                String id = connectionParameters.has("id") ? connectionParameters.getString("id") : null;
                String host = connectionParameters.has("host") ? connectionParameters.getString("host") : null;
                Integer port = connectionParameters.has("port") && !StringUtils.isEmpty(connectionParameters.getString("port")) ? connectionParameters.getInt("port") : null;
                Boolean isConnected = connectionParameters.has("isConnected") && connectionParameters.getBoolean("isConnected");
                String dbName = connectionParameters.has("dbName") ? connectionParameters.getString("dbName") : null;
                String user = connectionParameters.has("user") ? connectionParameters.getString("user") : null;
                String password = connectionParameters.has("password") ? connectionParameters.getString("password") : null;
                Integer timeout = connectionParameters.has("timeout") && !StringUtils.isEmpty(connectionParameters.getString("timeout")) ? connectionParameters.getInt("timeout") : null;
                Integer weight = connectionParameters.has("weight") && !StringUtils.isEmpty(connectionParameters.getString("weight")) ? connectionParameters.getInt("weight") : null;

                RedisConnection connection = new RedisConnection(id);

                connection.setHost(host);
                connection.setPort(port);
                connection.isConnected(isConnected);
                connection.setDbName(dbName);
                connection.setUser(user);
                connection.setPassword(password);
                connection.setTimeout(timeout);
                connection.setWeight(weight);
                JSONObject jsonAnswer = new JSONObject();
//                if (!databaseConnector.testConnection(connection)) {
//                    connection.isConnected(false);
//                    jsonAnswer.put("connectionVerified", false);
//                } else {
//                    jsonAnswer.put("connectionVerified", true);
//                }
                databaseConnector.addEditConnection(connection, false);
                jsonAnswer.put("success", "Connection successfully added");
                logger.info("Successfully created RedisDB connection: " + id);
                return Response.status(Response.Status.OK).entity(jsonAnswer.toString()).build();
            }
        } catch(JSONException e) {
            logger.error("Cannot parse json data : {}", data);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Cannot parse json data\"}").build();
        }
    }


  }