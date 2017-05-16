package org.jahia.modules.databaseConnector.api;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.jahia.modules.databaseConnector.api.filters.HeadersResponseFilter;
import org.jahia.modules.databaseConnector.util.Utils;
import org.osgi.framework.BundleContext;

/**
 * @author alexander karmanov on 2017-05-16.
 */
public class DCApiApplication extends ResourceConfig {

    public DCApiApplication(BundleContext context) {
        super(
                DCApi.class,
                JacksonJaxbJsonProvider.class,
                HeadersResponseFilter.class,
                MultiPartFeature.class,
                LoggingFilter.class
        );
    }
}
