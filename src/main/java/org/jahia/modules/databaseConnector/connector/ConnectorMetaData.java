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
package org.jahia.modules.databaseConnector.connector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by stefan on 2017-04-26.
 */
public class ConnectorMetaData {
    protected final String databaseType;
    protected final String displayName;
    protected final String entryPoint;
    protected final String moduleName;
    protected final String registryClassName;

    public ConnectorMetaData(final String databaseType,
                             final String displayName,
                             final String entryPoint,
                             final String moduleName,
                             final String registryClassName) {
        this.databaseType = databaseType;
        this.displayName = displayName;
        this.entryPoint = entryPoint;
        this.moduleName = moduleName;
        this.registryClassName = registryClassName;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getRegistryClassName() {
        return registryClassName;
    }

    @JsonIgnore
    public String getJson() {
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonMongoDbConnections = mapper.valueToTree(this);
        return jsonMongoDbConnections != null ? jsonMongoDbConnections.toString() : null;
    }
}
