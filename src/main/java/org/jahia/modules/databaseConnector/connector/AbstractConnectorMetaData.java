package org.jahia.modules.databaseConnector.connector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by stefan on 2017-04-26.
 */
public abstract class AbstractConnectorMetaData {
    protected final String databaseType;
    protected final String displayName;
    protected final String entryPoint;
    protected final String moduleName;

    public AbstractConnectorMetaData(final String databaseType,
                                     final String displayName,
                                     final String entryPoint,
                                     final String moduleName) {
        this.databaseType = databaseType;
        this.displayName = displayName;
        this.entryPoint = entryPoint;
        this.moduleName = moduleName;
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

    @JsonIgnore
    public String getJson() {
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonMongoDbConnections = mapper.valueToTree(this);
        return jsonMongoDbConnections != null ? jsonMongoDbConnections.toString() : null;
    }
}
