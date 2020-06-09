import { databaseConnector } from '../page-object/database-connector.page'
import { databaseConnectorImport } from "../page-object/database-connector.import";

describe('elastic search test', () => {
    it('imports a connection successfully', function () {
        databaseConnector.goTo()
        databaseConnectorImport.importConnection("esImportConn1-Export.txt")
            .verifyValidConnectionMessage()
    })
})
