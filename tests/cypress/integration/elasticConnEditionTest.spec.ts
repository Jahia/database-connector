import { databaseConnector } from '../page-object/database-connector.page'

describe('elastic search test', () => {

    var connId;

    before(('Create connection'), function (){
        connId = 'augm-search-conn'+ Date.now().toString();
        databaseConnector.goTo()
        databaseConnector
            .clickOnCreateNewConnection()
            .createNewElasticSearchConnection('elasticsearch', '9200', connId )
    })

    after(('Delete connection'), function (){
        databaseConnector.goTo()
        databaseConnector
            .clickOnDeleteConnection(connId)
            .confirmDeleteConnection()
    })

    it('edit a new connection successfully', function () {
        databaseConnector.goTo()
        databaseConnector
            .clickOnEditConnection(connId)
            .editElasticSearchConnection("myNewHost","9201")
            .verifyFailedConnectionMessage()
        databaseConnector.verifyConnectionExists('myNewHost', '9201', connId)
    })

    it('check id cannot be edited', function () {
        databaseConnector.goTo()
        databaseConnector
            .clickOnEditConnection(connId)
            .verifyElementIsreadOnly()
            .cancelEditConnection()
    })

})