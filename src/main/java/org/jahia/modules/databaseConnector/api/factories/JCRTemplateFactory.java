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
package org.jahia.modules.databaseConnector.api.factories;

import org.glassfish.hk2.api.Factory;
import org.jahia.modules.databaseConnector.api.SpringBeansAccess;
import org.jahia.services.content.JCRTemplate;

/**
 * Provider for JCRTemplate
 *
 * @author stefan on 2016-05-02.
 */
public class JCRTemplateFactory implements Factory<JCRTemplate> {

    @Override
    public JCRTemplate provide() {
        return SpringBeansAccess.getInstance().getJcrTemplate();
    }

    @Override
    public void dispose(JCRTemplate instance) {
        // nothing
    }
}