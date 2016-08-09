package org.jahia.modules.databaseConnector.connection;

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

    REDIS("RedisDB"), MONGO("MongoDB");

    public final static String DATABASE_TYPE_KEY = "databaseType";
    private final String displayName;

    DatabaseTypes(String displayName) {
        this.displayName = displayName;
    }

    public static Set<DatabaseTypes> getAllDatabaseTypes() {
        return new TreeSet<>(Arrays.asList(values()));
    }

    public String getDisplayName() {
        return displayName;
    }
}
