// import { databaseConnector } from '../page-object/database-connector.page'
// import { databaseConnectorImport } from '../page-object/database-connector.import'

// describe('import elastic search connections', () => {
//     afterEach(() => {
//         databaseConnector.cleanUp()
//     })

//     it('imports a connection successfully', function () {
//         databaseConnector.goTo()
//         databaseConnectorImport.importConnection('esImportConn1-Export.txt').verifyValidConnectionMessage()
//     })

//     // TODO: Enable when BACKLOG-14235 is fixed
//     // it('should fail to import connection', function () {
//     //     databaseConnector.goTo()
//     //     databaseConnectorImport.importConnection("badImportConn.txt")
//     //         .verifyInvalidConnectionMessage()
//     // })

//     it('imports multiple connections successfully', function () {
//         databaseConnector.goTo()
//         databaseConnectorImport.importConnection('esMultipleConn.txt').verifyValidConnectionMessage()
//         databaseConnectorImport.verifyNumberOfImportedConnections(4)
//     })
// })
