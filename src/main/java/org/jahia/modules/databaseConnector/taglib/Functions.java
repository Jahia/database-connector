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
package org.jahia.modules.databaseConnector.taglib;


import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.modules.databaseConnector.connection.DatabaseConnectorManager;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.query.QueryResultWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.render.View;
import org.jahia.services.render.scripting.Script;
import org.jahia.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

/**
 * Tag lib function to get angular templates
 */

public class Functions {

    private static Logger logger = LoggerFactory.getLogger(Functions.class);

    /**
     * This method get the path of the element after adding the module path with base and context.
     *
     * @param endOfUri      end of the path to the element
     * @param renderContext current render context
     * @return              the full path of the element
     */
    public static String addDatabaseConnectorModulePath(String endOfUri, RenderContext renderContext) {

        View currentView = ((Script) renderContext.getRequest().getAttribute("script")).getView();
        JahiaTemplatesPackage ffTemplatePackageModule = currentView.getModule();
        URLGenerator urlGenerator = renderContext.getURLGenerator();
        String path = urlGenerator.getContext() + (renderContext.getWorkspace().equals(Constants.EDIT_WORKSPACE) ? urlGenerator.getBasePreview() : urlGenerator.getBaseLive());
        try {
            JCRSessionWrapper session = renderContext.getMainResource().getNode().getSession();
            session.getNode(ffTemplatePackageModule.getRootFolderPath() + "/" + ffTemplatePackageModule.getVersion().toString() + "/templates/contents" + endOfUri);
            path += ffTemplatePackageModule.getRootFolderPath() + "/" + ffTemplatePackageModule.getVersion().toString() + "/templates/contents";
            path += endOfUri + ".html.ajax";
        } catch (RepositoryException e){
            try {
                JCRSessionWrapper session = renderContext.getMainResource().getNode().getSession();
                QueryResultWrapper execute = session.getWorkspace().getQueryManager().createQuery("select * from [fcmix:directiveDefinition] where localname() = '" + StringUtils.substringAfterLast(endOfUri, "/") + "'", Query.JCR_SQL2).execute();
                if (execute.getNodes().hasNext()) {
                    path += execute.getNodes().nextNode().getPath() + ".html.ajax";
                }
            } catch (RepositoryException e1) {
                return WebUtils.escapePath(path);
            }
        }
        try {
            return WebUtils.escapePath(path)+"?jsite="+renderContext.getSite().getIdentifier();
        } catch (RepositoryException e) {
            return WebUtils.escapePath(path);
        }
    }
}
