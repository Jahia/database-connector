package org.jahia.modules.databaseConnector.dsl;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.nodetypes.ExtendedNodeType;

import java.net.URL;

/**
 * Created by stefan on 2016-05-26.
 */
public interface DSLExecutor {

    /**
     * @param filename Groovy file to execute.
     * @param handler Handler that provides implementations for methos and properties the script references.
     */
    void execute(URL filename, DSLHandler handler);
}
