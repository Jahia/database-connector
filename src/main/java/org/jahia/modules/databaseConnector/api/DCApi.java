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
package org.jahia.modules.databaseConnector.api;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.modules.databaseConnector.api.impl.DirectivesRetriever;
import org.jahia.modules.databaseConnector.connection.DatabaseConnectionRegistry;
import org.jahia.modules.databaseConnector.services.DatabaseConnectorService;
import org.jahia.modules.databaseConnector.api.impl.DatabaseConnector;
import org.jahia.modules.databaseConnector.connection.AbstractConnection;
import org.jahia.modules.databaseConnector.util.Utils;
import org.jahia.settings.SettingsBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;

import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author stefan on 2016-05-02.
 */
@Path("/dbconn")
@Produces({"application/hal+json"})
public class DCApi extends DatabaseConnectionAPI {
    private static final Logger logger = getLogger(DCApi.class);

    public DCApi() {
        super(DCApi.class);
    }

    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHello() {
        return Response.status(Response.Status.OK).entity("{\"success\":\"Successfully setup DCApi\"}").build();
    }

    @GET
    @Path("/connectorsmetadata")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConnectorsMetaData() {
        try {
            return Response.status(Response.Status.OK).entity(getDatabaseConnector().getConnectorsMetaData()).build();
        } catch (JSONException ex) {
            logger.error("Failed to retrieve database types", ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Failed to retrieve database types\"}").build();
        }
    }

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response importTest(InputStream source) {
        JSONObject jsonAnswer = new JSONObject();
        try {
            Map results = getDatabaseConnector().importConnections(source);
            jsonAnswer.put("results", results);
            return Response.status(((Map)results.get("report")).get("status").toString().equals("success") ? Response.Status.OK : Response.Status.BAD_REQUEST).entity(jsonAnswer.toString()).build();
        } catch (JSONException ex) {
            logger.error("Failed to perform import", ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Failed to perform import\"}").build();
        }
    }

    @POST
    @Path("/export/{multipleconnections}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("text/plain")
    public Response exportConnection(String data, @PathParam("multipleconnections") boolean multipleConnections) {

        try {
            String exportName = null;
            if (StringUtils.isEmpty(data)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Missing Data\"}").build();
            }
            JSONObject jsonObject = new JSONObject(data);
            if (!multipleConnections) {
                Iterator databaseTypes = jsonObject.keys();
                while (databaseTypes.hasNext()) {
                    String databaseType = (String) databaseTypes.next();
                    JSONArray connections = jsonObject.getJSONArray(databaseType);
                    exportName = "DBConnector-" + connections.get(0) + "_" + databaseType + "_Export";
                }
            } else {
                exportName = "DBConnectorConnectionsExport";
            }
            File exportedConnections = getDatabaseConnector().exportConnections(jsonObject);
            Response.ResponseBuilder response;
            if (exportedConnections != null) {
                response = Response.ok(exportedConnections);
                response.type("text/plain").header("Content-Disposition", "attachment; filename=" + exportName + ".txt");
            } else {
                response = Response.serverError();
            }
            try {
                return response.build();
            } finally {
                if(exportedConnections!=null) {
                    FileUtils.forceDeleteOnExit(exportedConnections);
                }
            }
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid JSON object\"}").build();
        } catch (RepositoryException ex) {
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

    @POST
    @Path("/reimport/{multiple}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response reImportConnections(@PathParam("multiple") boolean multiple, String data) {
        try {
            JSONObject jsonAnswer = new JSONObject();
            if (multiple) {
                Map<String, LinkedList> importResults = new LinkedHashMap<>();
                LinkedList failed = new LinkedList();
                LinkedList success = new LinkedList();
                JSONArray connectionsToImport = new JSONArray(data);
                for (int i = 0; i < connectionsToImport.length(); i++) {
                    JSONObject connectionJsonObject = connectionsToImport.getJSONObject(i);
                    Map<String, Object> result = new LinkedHashMap<>();
                    if (connectionJsonObject.has("databaseType")) {
                        String databaseType = connectionJsonObject.getString("databaseType");
                        DatabaseConnectionRegistry databaseConnectionRegistry = (DatabaseConnectionRegistry) Utils.getService(databaseType, getContext());
                        databaseConnectionRegistry.buildConnectionMapFromJSON(result, connectionJsonObject);
                        if (result != null && result.containsKey("connectionStatus") && result.get("connectionStatus").equals("success")) {
                            AbstractConnection connection = ((AbstractConnection) result.get("connection"));
                            if (connection.isConnected() && !getDatabaseConnector().testConnection(connection)) {
                                connection.isConnected(false);
                            }
                            if (getDatabaseConnector().addEditConnection(connection, false)) {
                                result.put("connection", databaseConnectionRegistry.buildConnectionMapFromConnection(connection));
                                success.push(result);
                            } else {
                                result.put("connection", databaseConnectionRegistry.buildConnectionMapFromConnection(connection));
                                failed.push(result);
                            }
                        } else {
                            failed.push(result);
                        }
                    } else {
                        result.put("connectionStatus", "failed");
                        failed.push(result);
                    }
                }
                importResults.put("failed", failed);
                importResults.put("success", success);
                jsonAnswer.put("connections", importResults);
            } else {
                JSONObject connectionJsonObject = new JSONObject(data);
                Map<String, Object> result = new LinkedHashMap<>();
                if (connectionJsonObject.has("databaseType")) {
                    String databaseType = connectionJsonObject.getString("databaseType");
                    DatabaseConnectionRegistry databaseConnectionRegistry = (DatabaseConnectionRegistry) Utils.getService(databaseType, getContext());
                    databaseConnectionRegistry.buildConnectionMapFromJSON(result, connectionJsonObject);
                    if (result != null && result.containsKey("connectionStatus") && result.get("connectionStatus").equals("success")) {
                        AbstractConnection connection = (AbstractConnection) result.get("connection");
                        if (connection.isConnected() && !getDatabaseConnector().testConnection(connection)) {
                            connection.isConnected(false);
                        }
                        if (getDatabaseConnector().addEditConnection(connection, false)) {
                            jsonAnswer.put("success", databaseConnectionRegistry.buildConnectionMapFromConnection(connection));
                        } else {
                            jsonAnswer.put("failed", databaseConnectionRegistry.buildConnectionMapFromConnection(connection));
                        }
                    } else {
                        return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Invalid json object!\"}").build();
                    }
                } else {
                    result.put("connectionStatus", "failed");
                    jsonAnswer.put("failed", result);
                }
            }
            return Response.status(Response.Status.OK).entity(jsonAnswer.toString()).build();

        } catch (JSONException e) {
            logger.error("Cannot parse json data : {}", data);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Cannot parse json data\"}").build();
        }
    }

    @GET
    @Path("/allconnections")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConnections() {
        try {
            return Response.status(Response.Status.OK).entity(getDatabaseConnector().getAllConnections()).build();
        } catch (JSONException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Cannot parse json data\"}").build();
        } catch (InstantiationException ex) {
            logger.error("Cannot instantiate connection class" + ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Cannot access connection\"}").build();
        } catch (IllegalAccessException ex) {
            logger.error("Cannot access connection class" + ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Cannot access connection\"}").build();
        }
    }

    @GET
    @Path("/alldirectives")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAvailableDirectives() {
        try {
            return Response.status(Response.Status.OK).entity(DirectivesRetriever.getAllDirectives().getJson()).build();
        } catch (RepositoryException e) {
            logger.error("Cannot get directives" + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Cannot get directives\"}").build();
        }
    }
}
