import { DatabaseConnectorBasePage } from './database-connector.base.page'

class DatabaseConnectorPopupPage extends DatabaseConnectorBasePage {
    elements = {
        elasticsearch: "[src*='elasticsearch']",
        enableConnection: "[ng-model*='isConnected']",
        host: "input[name='host']",
        port: "input[name='port']",
        id: "input[name='id']",
        createButton: "[ng-click*='createElasticSearchConnection']",
        testButton: "[ng-click*='testElasticSearchConnection()']",
        backButton: "[ng-click*='cancel()']",
        notification: 'md-toast',
    }

    createNewElasticSearchConnection(host: string, port: string, id: string) {
        super.getElementInIframe(this.elements.elasticsearch).click()
        // this.checkbox(this.elements.enableConnection, true)
        super.getElementInIframe(this.elements.host).type(host)
        super.getElementInIframe(this.elements.port).clear().type(port)
        super.getElementInIframe(this.elements.id).type(id)
        // necessary for run on circleci. Fancier and more complicated workaround can be done
        // but this is so small i'm leaving it as is
        // eslint-disable-next-line cypress/no-unnecessary-waiting
        cy.wait(100)
        super.getElementInIframe(this.elements.createButton).click()
        return this
    }

    verifyInvalidConnectionMessage() {
        return super
            .getIframeElement('database-connector', this.elements.notification)
            .should('contain', 'Connection is invalid!')
    }

    verifyValidConnectionMessage() {
        return super
            .getIframeElement('database-connector', this.elements.notification)
            .should('contain', 'Connection verification was successful!')
    }
}
export const databaseConnectorPopup = new DatabaseConnectorPopupPage()
