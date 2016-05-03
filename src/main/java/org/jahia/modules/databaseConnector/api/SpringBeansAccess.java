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

import org.jahia.services.content.JCRTemplate;

/**
 * @author stefan on 2016-05-02.
 */
public final class SpringBeansAccess {
    private final static SpringBeansAccess INSTANCE = new SpringBeansAccess();
    private JCRTemplate jcrTemplate;

    private SpringBeansAccess() {
    }

    public static SpringBeansAccess getInstance() {
        return INSTANCE;
    }

    //FACTORIES BEGIN

    public JCRTemplate getJcrTemplate() {
        return jcrTemplate;
    }

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }
}
