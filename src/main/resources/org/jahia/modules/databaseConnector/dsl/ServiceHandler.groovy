package org.jahia.modules.databaseConnector.dsl

import org.jahia.api.Constants
import org.jahia.data.templates.JahiaTemplatesPackage
import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRContentUtils
import org.jahia.services.content.JCRNodeWrapper
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.JCRTemplate
import org.jahia.services.content.nodetypes.ExtendedNodeType
import org.jahia.services.usermanager.JahiaUserManagerService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.jcr.RepositoryException

/**
 * @author alexander karmanov on 2017-04-21.
 */

class ServiceHandler implements DSLHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServiceHandler.class);
    def JCRTemplate jcrTemplate;

    def services(@DelegatesTo(DatabaseConnectorWizardParser) Closure cl) {
        parseDeclaration(cl)
    }

    def parseDeclaration(Closure cl) {
        def serviceType = new DatabaseConnectorWizardParser()
        def code = cl.rehydrate(serviceType, this, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        logger.info("Declared directive structure: " + serviceType.toString());
        def result = serviceType.contentMap
        createWizard result, cl.owner.currentPackage, cl.owner.currentNodeType
    }

    def createWizard(Map<String, Object> map, JahiaTemplatesPackage currentPackage, ExtendedNodeType currentNodeType) {
        assert map.containsKey("label")
        assert map.containsKey("databaseType")
        jcrTemplate.doExecuteWithSystemSessionAsUser(JahiaUserManagerService.instance.lookupRootUser().jahiaUser, Constants.EDIT_WORKSPACE, Locale.ENGLISH, new JCRCallback() {
            @Override
            Object doInJCR(JCRSessionWrapper jcrSessionWrapper) throws RepositoryException {
                JCRNodeWrapper bundleRootNode = jcrSessionWrapper.getNode(currentPackage.rootFolderPath + "/" + currentPackage.getVersion().toString())
                def rNodeWrapper = bundleRootNode.getNode("templates").getNode("contents")
                if (!rNodeWrapper.hasNode("database-connector-services")) {
                    rNodeWrapper.addNode("database-connector-services","jnt:contentFolder")
                }
                def serviceRootNode = rNodeWrapper.getNode("database-connector-services")
                def nodeName = JCRContentUtils.generateNodeName(map["label"], 32)

                if (serviceRootNode.hasNode(nodeName)) {
                    //Reintroduce check for development if necessary
                    /*if (settingsBean.isDevelopmentMode()) {
                        directiveRootNode.getNode(nodeName).remove()
                        jcrSessionWrapper.save()
                    } else {
                        return null;
                    }*/
                    return null;
                }

                def node = serviceRootNode.addNode(nodeName, currentNodeType.name)
                node.setProperty(Constants.JCR_TITLE, map["label"])
                map.remove("label")

                node.setProperty("databaseType", map["databaseType"])
                map.remove("databaseType")

                jcrSessionWrapper.save()
                return null
            }
        })
    }
}
