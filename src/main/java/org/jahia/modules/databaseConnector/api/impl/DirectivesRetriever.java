package org.jahia.modules.databaseConnector.api.impl;

import org.jahia.api.Constants;
import org.jahia.modules.databaseConnector.serialization.models.AllDirectives;
import org.jahia.modules.databaseConnector.serialization.models.Directive;
import org.jahia.modules.databaseConnector.serialization.models.Directives;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author alexander karmanov on 2017-04-28.
 */
public class DirectivesRetriever {

    public static AllDirectives getAllDirectives() throws RepositoryException {
        JCRSessionWrapper session = JCRTemplate.getInstance().getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE);
        QueryManager qm = session.getWorkspace().getQueryManager();
        Query q = qm.createQuery("SELECT * FROM [dcmix:directivesDefinition]", Query.JCR_SQL2);
        NodeIterator ni = q.execute().getNodes();
        List<Directives> dr = new ArrayList<>();
        while (ni.hasNext()) {
            JCRNodeWrapper directives = (JCRNodeWrapper) ni.next();
            dr.add(getDirectives(directives));
        }

        return new AllDirectives(dr);
    }

    private static Directives getDirectives(JCRNodeWrapper node) throws RepositoryException {
        Directives directives = new Directives(node.getPropertyAsString(Constants.JCR_TITLE), null, null);
        JCRNodeWrapper connectionDirective = node.getNode("connectionDirective");
        directives.setConnectionDirective(
                new Directive(connectionDirective.getPropertyAsString("name"), connectionDirective.getPropertyAsString("tag"))
        );

        List<Directive> statusD = new ArrayList<>();
        JCRNodeWrapper statusDirectives = node.getNode("statusDirectives");
        List<JCRNodeWrapper> sd = JCRContentUtils.getChildrenOfType(statusDirectives, "dc:directiveDefinition");
        for (JCRNodeWrapper d : sd) {
            statusD.add(
                    new Directive(d.getPropertyAsString("name"), d.getPropertyAsString("tag"))
            );
        }
        directives.setStatusDirectives(statusD);
        return directives;
    }
}
