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

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.jahia.modules.databaseConnector.api.factories.DatabaseConnectorManagerFactory;
import org.jahia.modules.databaseConnector.api.factories.JCRTemplateFactory;
import org.jahia.modules.databaseConnector.connection.DatabaseConnectorManager;
import org.jahia.services.content.JCRTemplate;

/**
 * @author stefan on 2016-05-02.
 */
public class DCApiApplication extends ResourceConfig {
    public DCApiApplication() {
        this(JCRTemplateFactory.class,
                DatabaseConnectorManagerFactory.class);
    }

    DCApiApplication(final Class<? extends Factory<JCRTemplate>> jcrTemplateFactoryClass,
                     final Class<? extends Factory<DatabaseConnectorManager>> databaseConnectorManagerClass) {
        super(DCAPI.class,
                jcrTemplateFactoryClass,
                databaseConnectorManagerClass,
                JacksonJaxbJsonProvider.class,
                HeadersResponseFilter.class,
                MultiPartFeature.class);
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(jcrTemplateFactoryClass).to(JCRTemplate.class);
                bindFactory(databaseConnectorManagerClass).to(DatabaseConnectorManager.class);
            }
        });
    }
}
