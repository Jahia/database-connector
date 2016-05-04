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
    
    REDIS("Redis"), MONGO("MongoDB");

    private final String displayName;

    public final static String DATABASE_TYPE_KEY = "databaseType";

    DatabaseTypes(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Set<DatabaseTypes> getAllDatabaseTypes() {
        return new TreeSet<DatabaseTypes>(Arrays.asList(values()));
    }
}
