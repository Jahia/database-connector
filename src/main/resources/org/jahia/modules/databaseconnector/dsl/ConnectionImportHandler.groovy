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
        createWizard(result)
    }

    def createWizard(Map<String, Object> map) {
        databaseConnectorManager.importConnections(map);
    }
}