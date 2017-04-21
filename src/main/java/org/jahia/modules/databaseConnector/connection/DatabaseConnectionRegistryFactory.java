package org.jahia.modules.databaseConnector.connection;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Date: 11/6/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class DatabaseConnectionRegistryFactory {

    private static Map<String, DatabaseConnectionRegistry> registeredConnections = new LinkedHashMap<>();

    public static <T extends DatabaseConnectionRegistry> void makeDatabaseConnectionRegistry(DatabaseConnectionRegistry databaseConnectionRegistry) throws InstantiationException, IllegalAccessException {
        databaseConnectionRegistry.populateRegistry();
    }

    public static void registerConnectionType(String name, DatabaseConnectionRegistry databaseConnectionRegistry) {
        registeredConnections.put(name, databaseConnectionRegistry);
    }

    public static Map<String, DatabaseConnectionRegistry> getRegisteredConnections() {
        return registeredConnections;
    }
}
