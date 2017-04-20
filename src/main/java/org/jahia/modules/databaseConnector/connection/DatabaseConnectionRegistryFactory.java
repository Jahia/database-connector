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

    private static Map<String, Class> registeredConnections = new LinkedHashMap<>();

    public static <T extends DatabaseConnectionRegistry> DatabaseConnectionRegistry makeDatabaseConnectionRegistry(Class self) throws InstantiationException, IllegalAccessException {
        T databaseConnectionRegistry = null;
        databaseConnectionRegistry = (T) self.newInstance();
        databaseConnectionRegistry.populateRegistry();
        return databaseConnectionRegistry;
    }

    public static boolean registerConnectionType(String name, Class registeringClass) {
        synchronized (registeredConnections) {
            if (!registeredConnections.containsKey(name)) {
                registeredConnections.put(name, registeringClass);
                return true;
            }
        }
        return false;
    }

    public static Map<String, Class> getRegisteredConnections() {
        return registeredConnections;
    }
}
