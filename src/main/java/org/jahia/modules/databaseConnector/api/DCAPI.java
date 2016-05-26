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

import org.jahia.modules.databaseConnector.api.impl.DatabaseConnector;
import org.jahia.modules.databaseConnector.api.subresources.MongoDB;
import org.jahia.modules.databaseConnector.connection.DatabaseConnectorManager;
import org.jahia.services.content.JCRTemplate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

    //SUBRESOURCES MAPPINGS
    @Path(MongoDB.MAPPING)
    public Class<MongoDB> getMongoDbSubResource() {
        return MongoDB.class;
    }

    @POST
    @Path("/export")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response exportConnection(String data) {
        return Response.status(Response.Status.OK).entity("{\"success\":\"The connections are exported\"}").build();
//        return Response.status(Response.Status.OK).entity(databaseConnector.isConnectionIdAvailable(connectionId, DatabaseTypes.MONGO)).build();
    }
}
