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

import org.jahia.modules.databaseConnector.connection.DatabaseConnectorManager;
import org.jahia.modules.databaseConnector.api.impl.DatabaseConnector;
import org.jahia.modules.databaseConnector.connection.DatabaseTypes;
import org.jahia.services.content.JCRTemplate;
import org.json.JSONException;
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
    @Path("/getconnection/{databaseId}")
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
    @Path("/addeditconnection/{isEdition}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addEditConnection(@PathParam("isEdition") Boolean isEdition, String data) {
        try {
            databaseConnector.addEditConnection(data, isEdition);
            return Response.status(Response.Status.OK).entity("{\"success\": \"" + (isEdition ? "Connection successfully edited" : "Successfully connected to database") + "\"}").build();
        } catch(JSONException e) {
            logger.error("Cannot parse json data : {}", data);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Cannot parse json data\"}").build();
        } catch (UnknownHostException ex) {
            logger.error("Cannot add connection: {}", data);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Invalid connection parameters\"}").build();
        }
    }

    @DELETE
    @Path("/removeconnection/{connectionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeConnection(@PathParam("connectionId") String connectionId) {
        databaseConnector.removeConnection(connectionId, DatabaseTypes.MONGO);
        return Response.status(Response.Status.OK).entity("{\"success\": \"Successfully removed database connection\"}").build();
    }

    @PUT
    @Path("/connect/{connectionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response connect(@PathParam("connectionId") String connectionId) {
        databaseConnector.updateConnection(connectionId, DatabaseTypes.MONGO, true);
        return Response.status(Response.Status.OK).entity("{\"success\": \"Successfully connected to database\"}").build();
    }

    @PUT
    @Path("/disconnect/{connectionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response disconnect(@PathParam("connectionId") String connectionId) {
        databaseConnector.updateConnection(connectionId, DatabaseTypes.MONGO, false);
        return Response.status(Response.Status.OK).entity("{\"success\": \"Successfully disconnected from database\"}").build();
    }
}
