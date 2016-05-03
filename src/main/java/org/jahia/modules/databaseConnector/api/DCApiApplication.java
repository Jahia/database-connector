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

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.jahia.modules.databaseConnector.api.factories.JCRTemplateFactory;
import org.jahia.services.content.JCRTemplate;

/**
 * @author stefan on 2016-05-02.
 */
public class DCApiApplication extends ResourceConfig{
    public DCApiApplication() {
        this(JCRTemplateFactory.class);
    }

    DCApiApplication(final Class<? extends Factory<JCRTemplate>> jcrTemplateFactoryClass) {
        super(DCAPI.class,
                jcrTemplateFactoryClass,
                JacksonJaxbJsonProvider.class,
                HeadersResponseFilter.class,
                MultiPartFeature.class);
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(jcrTemplateFactoryClass).to(JCRTemplate.class);
            }
        });
    }
}
