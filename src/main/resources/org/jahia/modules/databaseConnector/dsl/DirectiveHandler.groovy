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

//    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, service = JCRTemplate.class)
//    public void setJCRTemplate(JCRTemplate jcrTemplate) {
//        this.jcrTemplate = jcrTemplate;
//    }

    def directive(@DelegatesTo(DatabaseConnectorWizardParser) Closure cl) {
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
        assert map.containsKey("tag")
        assert map.containsKey("views")
        jcrTemplate.doExecuteWithSystemSessionAsUser(JahiaUserManagerService.instance.lookupRootUser().jahiaUser, Constants.EDIT_WORKSPACE, Locale.ENGLISH, new JCRCallback() {
            @Override
            Object doInJCR(JCRSessionWrapper jcrSessionWrapper) throws RepositoryException {
                JCRNodeWrapper bundleRootNode = jcrSessionWrapper.getNode(currentPackage.rootFolderPath + "/" + currentPackage.getVersion().toString())
                def rNodeWrapper = bundleRootNode.getNode("templates").getNode("contents")
                if (!rNodeWrapper.hasNode("database-connector-directive")) {
                    rNodeWrapper.addNode("database-connector-directive","jnt:contentFolder")
                }
                def directiveRootNode = rNodeWrapper.getNode("database-connector-directive")
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

                def views
                if (map.get("views") instanceof List) {
                    views = (List<String>) map.get("views")
                } else {
                    views = [map.get("views")]
                }
                node.setProperty("views", views.toArray(new String[views.size()]))
                map.remove("views")

                node.setProperty("tag", map["tag"])
                map.remove("tag")

                jcrSessionWrapper.save()
                return null
            }
        })
    }
}
