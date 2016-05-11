package org.jahia.modules.databaseConnector.api.impl;
import org.jahia.services.content.JCRTemplate;
import org.slf4j.Logger;

/**
 * @author donnylam on 2016-05-03.
 */
public abstract class AbstractResource {
    private Logger logger;
    private JCRTemplate jcrTemplate;

    protected AbstractResource(JCRTemplate jcrTemplate, Logger logger) {
        this.jcrTemplate = jcrTemplate;
        this.logger = logger;
    }

}
