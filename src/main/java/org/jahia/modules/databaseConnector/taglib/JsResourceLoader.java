/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group. All rights reserved.
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
package org.jahia.modules.databaseConnector.taglib;

import org.apache.commons.lang.StringUtils;
import org.jahia.modules.databaseConnector.connection.DatabaseConnectorManager;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.scripting.Script;
import org.jahia.taglibs.AbstractJahiaTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Used to generate resource loading tag
 *
 * @author alexander karmanov on 2015-10-15.
 */
public class JsResourceLoader extends AbstractJahiaTag {

    private static Logger logger = LoggerFactory.getLogger(JsResourceLoader.class);

    public JsResourceLoader() {}

    @Override
    public int doEndTag() throws JspException {
        writeResourceTag(getRenderContext());
        return super.doEndTag();
    }

    /**
     * Calls definition service and returns file path for the generated js file.
     *
     * @param renderContext
     * @return file path (String/NULL)
     */
    private String getResourcePath(RenderContext renderContext) {
        DatabaseConnectorManager dcm = DatabaseConnectorManager.getInstance();
        try {
            return dcm.getAngularConfigFilePath(renderContext);
        } catch (Exception e) {
            logger.error("Failed to get file path for generated js resource: " + e.getMessage());
            return null;
        }
    }

    private Long getResourceTimestamp(RenderContext renderContext) {
        DatabaseConnectorManager dcm = DatabaseConnectorManager.getInstance();
        try {
            return dcm.getAngularConfigFileTimestamp(renderContext);
        } catch (Exception e) {
            logger.error("Failed to get file path for generated js resource: " + e.getMessage());
            return null;
        }
    }

    /**
     * Creates and writes to page jahia:resource tag with generated js file (in DefinitionService) as the resource
     * if it exists.
     *
     * @param renderContext
     */
    private void writeResourceTag(RenderContext renderContext) {
        String filePath = getResourcePath(renderContext);
        if (filePath != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("<jahia:resource type=\"javascript\"");
            try {
                logger.info("Generated resources file path: " + filePath+ " transformed too: " + StringUtils.substringAfter(filePath, "generated-resources").replaceAll(File.pathSeparator.equals("\\")?"\\\\":"/","/"));
                builder.append(" path=\"").append(URLEncoder.encode(renderContext.getURLGenerator().getContext() + "/generated-resources/" + new File(filePath).getName(), "UTF-8")).append("?ts=").append(getResourceTimestamp(renderContext)).append("\"");
            } catch (UnsupportedEncodingException e) {
                logger.error("Failed to encode file path: " + e.getMessage());
            }
            builder.append(" key=\"\"");
            builder.append(" />\n");
            try {
                pageContext.getOut().print(builder.toString());
            } catch (IOException e) {
                logger.error("Failed to write generated jahia:resource tag to page: " + e.getMessage());
            }
        }
    }
}
