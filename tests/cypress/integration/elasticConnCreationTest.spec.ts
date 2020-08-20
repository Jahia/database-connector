import { databaseConnector } from '../page-object/database-connector.page'

describe('elastic search connection creation', () => {
    afterEach(() => {
        databaseConnector.cleanUp()
    })

    it('creates a new connection successfully', function () {
        databaseConnector.goTo()
        databaseConnector
            .clickOnCreateNewConnection()
            .createNewElasticSearchConnection('elasticsearch', '9200', 'augmented-search-conn')
            .verifyValidConnectionMessage()
        databaseConnector.verifyConnectionExists('elasticsearch', '9200', 'augmented-search-conn')
    })
})
