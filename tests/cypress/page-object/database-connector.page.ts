import { DatabaseConnectorBasePage } from './database-connector.base.page'
import { databaseConnectorPopup } from './database-connector.popup.page'
import { deleteConnectorPopup } from './delete-connector.popup.page'

class DatabaseConnectorPage extends DatabaseConnectorBasePage {
    elements = {
        newConnection: '.connection-containerMain button',
        connection3Dots: "[ng-click*='open']",
        menuContainer: "[id*='menu_container'][aria-hidden='false']",
        editConnection: "[ng-click*='editConnection']",
        deleteConnection: "[ng-click*='openDeleteConnection']",
        databaseType: "[src*='elasticsearch']",
        connectionId: "[message-key*='connectionName']", // Use parent with this element
        host: "[message-key*='host']", // Use parent with this element
        port: "[message-key*='port']", // Use parent with this element
    }

    goTo() {
        cy.goTo('/jahia/administration/database-connector')
    }

    clickOnCreateNewConnection() {
        this.getElementInIframe(this.elements.newConnection).click()
        return databaseConnectorPopup
    }

    clickOnEditConnection(connectionId:string) {
        this.getByText("md-card",connectionId).find("i").click();
        this.getElementInIframe(this.elements.menuContainer).find(this.elements.editConnection).click();
        return databaseConnectorPopup
    }

    clickOnDeleteConnection(connectionId:string) {
        this.getByText("md-card",connectionId).find("i").click();
        this.getElementInIframe(this.elements.menuContainer).find(this.elements.deleteConnection).click();
        return deleteConnectorPopup
    }

    verifyConnectionExists(host: string, port: string, id: string) {
        this.getElementInIframe(this.elements.connectionId).parent().should('contain', id)
        this.getElementInIframe(this.elements.host).parent().should('contain', host)
        this.getElementInIframe(this.elements.port).parent().should('contain', port)
    }
}
export const databaseConnector = new DatabaseConnectorPage()