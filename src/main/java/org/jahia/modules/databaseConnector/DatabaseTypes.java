package org.jahia.modules.databaseConnector;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * Date: 11/1/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public enum DatabaseTypes {
    
    NEO4J("Neo4j"), REDIS("Redis"), MONGO("MongoDB");

    private final String displayName;

    DatabaseTypes(String displayName) {
        this.displayName = displayName;
    }

    public static String getKey() {
        return "databaseType";
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Set<DatabaseTypes> getAllDatabaseTypes() {
        return new TreeSet<DatabaseTypes>(Arrays.asList(values()));
    }
}
