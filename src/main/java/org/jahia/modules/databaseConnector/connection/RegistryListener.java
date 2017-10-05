package org.jahia.modules.databaseConnector.connection;

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.jahia.modules.databaseConnector.services.DatabaseConnectionRegistry;
import org.jahia.services.content.*;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.LinkedList;
import java.util.List;

public class RegistryListener extends DefaultEventListener implements InitializingBean, BundleListener, BundleContextAware, ExternalEventListener {
    private static final Logger logger = LoggerFactory.getLogger(RegistryListener.class);

    private SettingsBean settingsBean;
    private JCRTemplate jcrTemplate;
    private Bundle bundle;

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED;
    }

    @Override
    public void onEvent(EventIterator events) {
        while (events.hasNext()) {
            final Event event = events.nextEvent();
            try {
                if (settingsBean != null && !settingsBean.isProcessingServer() && isExternal(event) && event.getPath().startsWith("/settings/databaseConnector/")) {
//                    System.out.println("******#" + getDatabaseConnectionRegistryServices().get(0).getConnection("myconnection").dbName);
//                    System.out.println("**********---isExternal: " + isExternal(event));
//                    System.out.println("********** " + event.toString());
                    switch (event.getType()) {
                        case Event.NODE_ADDED :
                            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                                @Override
                                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                    System.out.println("****NAME" + session.getNode(event.getPath()).getName());
                                    return null;
                                }
                            });
                        break;

                        case Event.NODE_REMOVED :
                        break;

                        case Event.PROPERTY_CHANGED :
                        case Event.PROPERTY_ADDED :
                        case Event.PROPERTY_REMOVED :
                        break;
                    }
                }
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        bundleContext.addBundleListener(this);
        this.bundle = bundleContext.getBundle();
    }

    @Override
    public void bundleChanged(BundleEvent bundleEvent) {
        //Register service when module restarts
        Bundle bundleEventBundle = bundleEvent.getBundle();
        //TODO get bundle dependencies and make sure that "database-connector" is there instead of connector
        if (bundleEventBundle.getSymbolicName().contains("connector") && bundleEvent.getType() == BundleEvent.STARTED) {
            logger.debug("Starting connection services registration process...");
            for (DatabaseConnectionRegistry databaseConnectionRegistry : getDatabaseConnectionRegistryServices()) {
                logger.info("\tRegistering services for: " + databaseConnectionRegistry.getConnectionDisplayName() + " using " + databaseConnectionRegistry.getClass());
                databaseConnectionRegistry.registerServices();
            }
            logger.debug("Registration process completed!");
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    private List<DatabaseConnectionRegistry> getDatabaseConnectionRegistryServices() {
        List<DatabaseConnectionRegistry> databaseConnectionRegistryServices = new LinkedList<>();
        try {
            ServiceReference[] serviceReferences = this.bundle.getBundleContext().getAllServiceReferences(DatabaseConnectionRegistry.class.getName(), null);
            if (serviceReferences != null) {
                for (ServiceReference serviceReference : serviceReferences) {
                    databaseConnectionRegistryServices.add((DatabaseConnectionRegistry) this.bundle.getBundleContext().getService(serviceReference));
                }
            }
        } catch (InvalidSyntaxException ex) {
            logger.error("Could not find service: " + ex.getMessage());
        }
        return databaseConnectionRegistryServices;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }
}
