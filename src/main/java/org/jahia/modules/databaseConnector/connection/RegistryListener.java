package org.jahia.modules.databaseConnector.connection;

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.modules.databaseConnector.services.DatabaseConnectionRegistry;
import org.jahia.services.content.*;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RegistryListener extends DefaultEventListener implements InitializingBean, BundleListener, BundleContextAware, ExternalEventListener {
    private static final Logger logger = LoggerFactory.getLogger(RegistryListener.class);

    private SettingsBean settingsBean;
    private JCRTemplate jcrTemplate;
    private JahiaTemplateManagerService templateManagerService;
    private Bundle bundle;

    @Override
    public String[] getNodeTypes() {
        return new String[]{"dcmix:databaseConnection"};
    }

    @Override
    public int getEventTypes() {
        return Event.PROPERTY_CHANGED + Event.NODE_REMOVED;
    }

    @Override
    public void onEvent(EventIterator events) {
        final Map<String, Map<String, Object>> nodeToPropertyValueMap = new HashMap<>();

        while (events.hasNext()) {
            final Event event = events.nextEvent();
            try {
                String[] nodePathParts = event.getPath().split("/");
                //i.e. /settings/databaseConnector/<connectionName>
                String nodePath = "/".concat(nodePathParts[1]).concat("/").concat(nodePathParts[2]).concat("/").concat(nodePathParts[3]);
                if (isExternal(event) && event.getPath().startsWith("/settings/databaseConnector/")) {
                    if (!nodeToPropertyValueMap.containsKey(nodePath)) {
                        nodeToPropertyValueMap.put(nodePath, new HashMap<String, Object>());
                    }
                    //Note that the value is the same for all entries as it is not used in anyway, the property name matters
                    switch (event.getType()) {
                        case Event.PROPERTY_CHANGED :
                            nodeToPropertyValueMap.get(nodePath).put(nodePathParts[4], "modified");
                        break;
                    }
                }
                else if (!nodeToPropertyValueMap.containsKey(nodePath)) {
                    //This means that we don't want to consider that case since event is not external
                    nodeToPropertyValueMap.put(nodePath, null);
                }
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
        System.out.println("***While done");
        try {
            jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Object>() {
                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    for (Map.Entry<String, Map<String, Object>> entry: nodeToPropertyValueMap.entrySet()) {
                        String connectionPath = entry.getKey();
                        if (nodeToPropertyValueMap.get(connectionPath) != null) {
                            if (nodeToPropertyValueMap.get(connectionPath).size() == 0) {
                                //Node was removed, we want to remove connection
                                boolean removalResult = findAndRemoveConnectionByPath(connectionPath);
                                System.out.println("**** Node removed: " + connectionPath);
                                System.out.println("**** Node removed: " + removalResult);
                            }
                            else if (nodeToPropertyValueMap.get(connectionPath).containsKey("jcr:created") || nodeToPropertyValueMap.get(connectionPath).containsKey("jcr:lastModified")){
                                //Node was created or modified so we want to either create a connection or edit existing one
                                //Note that there are two batches of events in case a prop is modified: one that contains modified props and one that modifies lastModified date
                                //we want to only get the second batch to prevent unneeded work
                                JCRNodeWrapper connectionNode = session.getNode(connectionPath);
                                DatabaseConnectionRegistry registry = getRegistryForDatabaseType(connectionNode.getPropertyAsString(AbstractConnection.DATABASE_TYPE_PROPETRY));
                                if (registry != null) {
                                    AbstractConnection connection = registry.nodeToConnection(connectionNode);
                                    registry.addEditConnectionNoStore(
                                            connection,
                                            !(registry.getConnection(connection.getOldId()) == null && registry.getConnection(connection.getOldId()) == null)
                                    );
                                }
                                System.out.println("**** Node created or modified: " + connectionPath);
                            }
                        }
                    }
                    session.save();
                    return null;
                }
            });
        } catch (RepositoryException e) {
            e.printStackTrace();
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
        if (bundleEvent.getType() == BundleEvent.STARTED && dependsOnDatabaseConnector(bundleEventBundle)) {
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
        for (DatabaseConnectionRegistry databaseConnectionRegistry : getDatabaseConnectionRegistryServices()) {
            logger.debug("\tRegistering services for: " + databaseConnectionRegistry.getConnectionDisplayName() + " using " + databaseConnectionRegistry.getClass());
            databaseConnectionRegistry.registerServices();
        }
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

    private DatabaseConnectionRegistry getRegistryForDatabaseType(String databaseType) {
        try {
            ServiceReference[] serviceReferences = this.bundle.getBundleContext().getAllServiceReferences(DatabaseConnectionRegistry.class.getName(), null);
            if (serviceReferences != null) {
                for (ServiceReference serviceReference : serviceReferences) {
                    DatabaseConnectionRegistry connectionRegistry = (DatabaseConnectionRegistry) this.bundle.getBundleContext().getService(serviceReference);
                    if (connectionRegistry.getConnectionType().equals(databaseType)) {
                        return connectionRegistry;
                    }
                }
            }
        } catch (InvalidSyntaxException ex) {
            logger.error("Could not find service: " + ex.getMessage());
        }
        return null;
    }

    private boolean findAndRemoveConnectionByPath(String connectionPath) {
        List<DatabaseConnectionRegistry> registries = getDatabaseConnectionRegistryServices();

        for (DatabaseConnectionRegistry registry : registries) {
            Map<String, AbstractConnection> availableConnections = registry.getRegistry();
            for (Map.Entry<String, AbstractConnection> entry: availableConnections.entrySet()) {
                AbstractConnection connection = availableConnections.get(entry.getKey());
                if (connection.getPath().equals(connectionPath)) {
                    registry.unregisterAndRemoveFromRegistry(connection.getId());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean dependsOnDatabaseConnector(Bundle bundle) {
        JahiaTemplatesPackage pack = templateManagerService.getTemplatePackage((String) bundle.getHeaders().get("Bundle-Name"));

        if (pack == null) return false;

        for (JahiaTemplatesPackage dependency : pack.getDependencies()) {
            if (dependency.getId().equals("database-connector")) {
                return true;
            }
        }
        return false;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }
}
