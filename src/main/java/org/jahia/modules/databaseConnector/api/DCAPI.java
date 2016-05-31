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
package org.jahia.modules.databaseConnector.api;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.modules.databaseConnector.api.impl.DatabaseConnector;
import org.jahia.modules.databaseConnector.api.subresources.MongoDB;
import org.jahia.modules.databaseConnector.connection.DatabaseConnectorManager;
import org.jahia.services.content.JCRTemplate;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author stefan on 2016-05-02.
 */
@Component
@Path("/databaseconnector")
@Produces({"application/hal+json"})
public class DCAPI {
    private static final Logger logger = getLogger(DCAPI.class);
    private DatabaseConnector databaseConnector;

    @Inject
    public DCAPI(JCRTemplate jcrTemplate, DatabaseConnectorManager databaseConnectorManager) {
        databaseConnector = new DatabaseConnector(jcrTemplate, databaseConnectorManager, logger);
    }

    //@TODO Remove when production ready
    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHello() {
        return Response.status(Response.Status.OK).entity("{\"success\":\"Successfully setup DCAPI\"}").build();
    }

    @GET
    @Path("/databasetypes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatabaseTypes() {
        return Response.status(Response.Status.OK).entity(databaseConnector.getDatabaseTypes()).build();
    }

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response importTest(InputStream source){
        JSONObject jsonAnswer = new JSONObject();
        try {
            jsonAnswer.put("results", databaseConnector.importConnections(source));
            return Response.status(Response.Status.OK).entity(jsonAnswer.toString()).build();
        } catch (JSONException ex) {
            logger.error("Failed to perform import", ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Failed to perform import\"}").build();
        }
    }

    //SUBRESOURCES MAPPINGS
    @Path(MongoDB.MAPPING)
    public Class<MongoDB> getMongoDbSubResource() {
        return MongoDB.class;
    }

    @POST
    @Path("/export")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("text/plain")
    public Response exportConnection(String data) {
        try {
            if (StringUtils.isEmpty(data)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Missing Data\"}").build();
            }
            JSONObject jsonObject = new JSONObject(data);
            File exportedConnections = databaseConnector.exportConnections(jsonObject);
            Response.ResponseBuilder response;
            if (exportedConnections != null){
                response = Response.ok(exportedConnections);
                response.type("text/plain").header("Content-Disposition", "attachment; filename=ExportedConnections.txt");
            }
            else {
                response = Response.serverError();
            }
            try {
                return response.build();
            } finally {
                FileUtils.forceDeleteOnExit(exportedConnections);
            }
        } catch(JSONException ex) {
            logger.error(ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid JSON object\"}").build();
        } catch(RepositoryException ex) {
            logger.error(ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Could not perform connection export\"}").build();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Could not perform connection export\"}").build();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Could not perform connection export\"}").build();
        }

    }
}
