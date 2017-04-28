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
import org.osgi.service.component.annotations.Component
import org.osgi.service.component.annotations.Reference
import org.osgi.service.component.annotations.ReferenceCardinality
import org.osgi.service.component.annotations.ReferencePolicy
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.jcr.RepositoryException

/**
 * @author alexander karmanov on 2017-04-21.
 */
@Component (service = DirectiveHandler.class)
class DirectiveHandler implements DSLHandler {
    private static final Logger logger = LoggerFactory.getLogger(DirectiveHandler.class);
    def JCRTemplate jcrTemplate;

    def directives(@DelegatesTo(DatabaseConnectorWizardParser) Closure cl) {
        parseDeclaration(cl)
    }

    def parseDeclaration(Closure cl) {
        def directiveType = new DatabaseConnectorWizardParser()
        def code = cl.rehydrate(directiveType, this, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        logger.info("Declared directive structure: " + directiveType.toString());
        def result = directiveType.contentMap
        createWizard result, cl.owner.currentPackage, cl.owner.currentNodeType
    }

    def createWizard(Map<String, Object> map, JahiaTemplatesPackage currentPackage, ExtendedNodeType currentNodeType) {
        assert map.containsKey("label")
        assert map.containsKey("statusDirectives")
        assert map.containsKey("connectionDirective")
        jcrTemplate.doExecuteWithSystemSessionAsUser(JahiaUserManagerService.instance.lookupRootUser().jahiaUser, Constants.EDIT_WORKSPACE, Locale.ENGLISH, new JCRCallback() {
            @Override
            Object doInJCR(JCRSessionWrapper jcrSessionWrapper) throws RepositoryException {
                JCRNodeWrapper bundleRootNode = jcrSessionWrapper.getNode(currentPackage.rootFolderPath + "/" + currentPackage.getVersion().toString())
                def rNodeWrapper = bundleRootNode.getNode("templates").getNode("contents")
                if (!rNodeWrapper.hasNode("database-connector-directives")) {
                    rNodeWrapper.addNode("database-connector-directives","jnt:contentFolder")
                }
                def directiveRootNode = rNodeWrapper.getNode("database-connector-directives")
                def nodeName = JCRContentUtils.generateNodeName(map["label"], 32)

                if (directiveRootNode.hasNode(nodeName)) {
                    //Reintroduce check for development if necessary
                    /*if (settingsBean.isDevelopmentMode()) {
                        directiveRootNode.getNode(nodeName).remove()
                        jcrSessionWrapper.save()
                    } else {
                        return null;
                    }*/
                    return null;
                }

                def node = directiveRootNode.addNode(nodeName, currentNodeType.name)
                node.setProperty(Constants.JCR_TITLE, map["label"])
                map.remove("label")

                //Need to save so that statusDirectives node is autocreated
                jcrSessionWrapper.save()

                def properties = map["statusDirectives"]
                JCRNodeWrapper statusDirectives = node.getNode("statusDirectives");
                properties.each { key, value ->
                    def directiveNode = statusDirectives.addNode(key, "dc:directiveDefinition")
                    if (value instanceof String) {
                        directiveNode.setProperty("name", key)
                        directiveNode.setProperty("tag", value)
                    }
                }
                map.remove("statusDirectives")

                JCRNodeWrapper connectionDirective = node.addNode("connectionDirective", "dc:directiveDefinition")
                connectionDirective.setProperty("name", "connectionDirective")
                connectionDirective.setProperty("tag", map["connectionDirective"])


                jcrSessionWrapper.save()
                return null
            }
        })
    }
}
