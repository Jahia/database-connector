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
 *     Copyright (C) 2002-2018 Jahia Solutions Group. All rights reserved.
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
package org.jahia.modules.databaseConnector.connection;

import org.jahia.modules.databaseConnector.api.impl.DatabaseConnector;
import org.jahia.modules.databaseConnector.services.DatabaseConnectorService;
import org.jahia.modules.databaseConnector.util.Utils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * @author alexander karmanov on 2017-05-16.
 */
public class DatabaseConnectionAPI {
    private BundleContext context;
    private DatabaseConnector databaseConnector;

    public DatabaseConnectionAPI(Class apiClass) {
        context = FrameworkUtil.getBundle(apiClass).getBundleContext();
    }

    public BundleContext getContext() {
        return context;
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }

    public DatabaseConnector getDatabaseConnector() {
        if (databaseConnector == null) {
            databaseConnector = getConnectorReference();
        }
        return databaseConnector;
    }

    public void setDatabaseConnector(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    private DatabaseConnector getConnectorReference() {
        return (DatabaseConnector) Utils.getService(DatabaseConnectorService.class.getName(), context);
    }
}
