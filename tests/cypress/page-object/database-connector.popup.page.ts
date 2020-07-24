import { DatabaseConnectorBasePage } from './database-connector.base.page'

class DatabaseConnectorPopupPage extends DatabaseConnectorBasePage {
    elements = {
        elasticsearch: "[src*='elasticsearch']",
        enableConnection: "[ng-model*='isConnected']",
        host: "input[name='host']",
        port: "input[name='port']",
        id: "input[name='id']",
        createButton: "[ng-click*='createElasticSearchConnection']",
        updateButton: "[ng-click*='editElasticSearchConnection']",
        testButton: "[ng-click*='testElasticSearchConnection()']",
        backButton: "[ng-click*='cancel()']",
        notification: 'md-toast',
    }

    createNewElasticSearchConnection(host: string, port: string, id: string) {
        cy.get(this.elements.elasticsearch).click()
        this.fillElasticSearchConnection(host, port)
        cy.get(this.elements.id).type(id)
        // necessary for run on circleci. Fancier and more complicated workaround can be done
        // but this is so small i'm leaving it as is
        // eslint-disable-next-line cypress/no-unnecessary-waiting
        cy.wait(100)
        cy.get(this.elements.createButton).click()
        return this
    }

    /**
     * Updating the connection with a host, port or both
     * @param host
     * @param port
     */
    editElasticSearchConnection(host = '', port = '') {
        this.fillElasticSearchConnection(host, port)
        // necessary for run on circleci. Fancier and more complicated workaround can be done
        // but this is so small i'm leaving it as is
        // eslint-disable-next-line cypress/no-unnecessary-waiting
        cy.wait(100)
        cy.get(this.elements.updateButton).click()
        return this
    }

    fillElasticSearchConnection(host = '', port = '') {
        if (host != '') {
            cy.get(this.elements.host).clear()
            // Only way it works consistently
            // eslint-disable-next-line cypress/no-unnecessary-waiting
            cy.wait(200)
            cy.get(this.elements.host).type(host)
        }
        if (port != '') {
            cy.get(this.elements.port).clear().type(port)
        }
    }

    cancelEditConnection() {
        cy.get(this.elements.backButton).click()
    }

    verifyElementIsreadOnly() {
        cy.get(this.elements.id).should('have.attr', 'readOnly', 'readonly')
        return this
    }

    verifyInvalidConnectionMessage() {
        return cy.get(this.elements.notification).should('contain', 'Connection is invalid!')
    }

    verifyFailedConnectionMessage() {
        return cy.get(this.elements.notification).should('contain', 'Connection verification failed!')
    }

    verifyValidConnectionMessage() {
        return cy.get(this.elements.notification).should('contain', 'Connection verification was successful!')
    }
}
export const databaseConnectorPopup = new DatabaseConnectorPopupPage()
