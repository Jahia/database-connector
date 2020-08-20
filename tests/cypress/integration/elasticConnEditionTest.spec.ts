import { databaseConnector } from '../page-object/database-connector.page'

describe('elastic search connection edit', () => {
    let connId: string

    beforeEach('Create connection', function () {
        connId = 'augm-search-conn' + Date.now().toString()
        databaseConnector.createConnection('myHost', '9201', connId)
    })

    afterEach('Delete connection', function () {
        databaseConnector.cleanUp()
    })

    it('edit a new connection with error', function () {
        databaseConnector.goTo()
        databaseConnector
            .clickOnEditConnection(connId)
            .editElasticSearchConnection('mynewhost', '9202')
            .verifyFailedConnectionMessage()
        databaseConnector.verifyConnectionExists('mynewhost', '9202', connId)
    })

    it('edit a new connection successfully', function () {
        databaseConnector.goTo()
        databaseConnector
            .clickOnEditConnection(connId)
            .editElasticSearchConnection('elasticsearch', '9200')
            .verifyValidConnectionMessage()
        databaseConnector.verifyConnectionExists('elasticsearch', '9200', connId)
    })

    it('check id cannot be edited', function () {
        databaseConnector.goTo()
        databaseConnector.clickOnEditConnection(connId).verifyElementIsreadOnly().cancelEditConnection()
    })
})
