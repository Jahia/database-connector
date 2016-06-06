package org.jahia.modules.databaseConnector.dsl

import org.jahia.modules.databaseConnector.connection.DatabaseConnectorManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ConnectionImportHandler implements DSLHandler {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionImportHandler.class);

    def DatabaseConnectorManager databaseConnectorManager

    def connection(@DelegatesTo(DatabaseConnectorWizardParser) Closure cl) {
        parseDeclaration(cl)
    }

    def parseDeclaration(Closure cl) {
        def parser = new DatabaseConnectorWizardParser()
        def code = cl.rehydrate(parser, this, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        logger.info("Declared connection structure: " + parser.toString());
        def result = parser.contentMap
        createWizard result, cl.owner.importedConnectionsResults
    }

    def createWizard(Map<String, Object> map, Map<String, Map> importedConnectionsResults) {
        //create the storage container for the connections of specific database type if it does not exist
        if (!importedConnectionsResults.containsKey(map.get("type"))) {
            //Create map to hold success connections and failed connections
            Map<String, Map> results = new LinkedHashMap<>()
            results.put("success", new LinkedList<>())
            results.put("failed", new LinkedList<>())
            importedConnectionsResults.put(map.get("type"), results)
        }
        map.put("id", map.get("identifier"))
        map.put("databaseType", map.get("type"))
        //Added connection import result into the appropriate database type linked hash map
        map = databaseConnectorManager.importConnection(map);
        importedConnectionsResults.get(map.get("type")).get(map.get("status")).add(map);
    }
}