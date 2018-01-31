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
 *     Copyright (C) 2002-2018 Jahia Solutions Group. All rights reserved.
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
package org.jahia.modules.databaseConnector.dsl;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.nodetypes.ExtendedNodeType;

import java.net.URL;
import java.util.Map;

/**
 * Created by stefan on 2016-05-26.
 */
public interface DSLExecutor {

    /**
     * @param filename Groovy file to execute.
     * @param handler  Handler that provides implementations for methos and properties the script references.
     */
    Map<String, Map> execute(URL filename, DSLHandler handler, Map<String, Map> map);

    /**
     * @param filename Groovy file to execute.
     * @param handler Handler that provides implementations for methos and properties the script references.
     */
    void execute(URL filename, DSLHandler handler, JahiaTemplatesPackage packageById, ExtendedNodeType type);
}
