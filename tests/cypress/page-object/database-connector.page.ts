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
        cy.goTo('/cms/adminframe/default/en/settings.database-connector.html')
    }

    clickOnCreateNewConnection() {
        cy.get(this.elements.newConnection).click()
        return databaseConnectorPopup
    }

    clickOnEditConnection(connectionId: string) {
        super.getByText('md-card', connectionId).find('i').click()
        cy.get(this.elements.menuContainer).find(this.elements.editConnection).click()
        return databaseConnectorPopup
    }

    clickOnDeleteConnection(connectionId: string) {
        this.getByText('md-card', connectionId).find('i').click()
        this.getElementInIframe(this.elements.menuContainer).find(this.elements.deleteConnection).click()
        return deleteConnectorPopup
    }

    verifyConnectionExists(host: string, port: string, id: string) {
        cy.get(this.elements.connectionId).parent().should('contain', id)
        cy.get(this.elements.host).parent().should('contain', host)
        cy.get(this.elements.port).parent().should('contain', port)
    }
    /**
     * delete all connections
     */
    cleanUp() {
        cy.request('GET', 'modules/dbconn/allconnections').then((response) => {
            // response.body is automatically serialized into JSON
            response.body.connections.forEach((card) => {
                cy.request('DELETE', 'modules/dbconn/elasticsearch7/remove/' + card.id)
            })
        })
    }

    /**
     * context menu of an existing connection
     * @param action - one of the options in the menu
     */
    contextMenu(action: Action) {
        switch (action) {
            case Action.DELETE:
                cy.get("[ng-click*='DeleteConnectionDialog']")
                this.deleteDialog(true)
                break
            case Action.DISCONNECT:
                //
                break
            case Action.DELETE:
                //
                break
            case Action.EXPORT:
                //
                break
            case Action.CONNECTION_STATISTICS:
                //
                break
            default:
                console.error('the action: ' + action + ' does not exist in the context menu')
                break
        }
    }

    deleteDialog(confirmed: boolean) {
        if (confirmed) {
            cy.get(".custom-confirm-dialog [message-key='dc_databaseConnector.label.delete']").click({ force: true })
        } else {
            cy.get('.md-dialog-content')
                .find("[message-key='dc_databaseConnector.label.cancel']")
                .click({ timeout: 2000 })
        }
    }
}

enum Action {
    DELETE,
    DISCONNECT,
    EDIT,
    EXPORT,
    CONNECTION_STATISTICS,
}

export const databaseConnector = new DatabaseConnectorPage()
