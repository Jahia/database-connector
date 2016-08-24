package org.jahia.modules.databaseConnector.connection;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.LinkedList;

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

    public static String getValueAsString(DatabaseTypes databaseType) {
        switch (databaseType) {
            case MONGO:
                return "MONGO";
            case REDIS:
                return "REDIS";
            default:
                return null;
        }
    }

    public static List<String> getValuesAsString() {
        List databaseTypes = new LinkedList();
        for (DatabaseTypes databaseType: DatabaseTypes.values()) {
            databaseTypes.add(getValueAsString(databaseType));
        }
        return databaseTypes;
    }
}
