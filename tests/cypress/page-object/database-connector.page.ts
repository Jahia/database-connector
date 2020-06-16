import { DatabaseConnectorBasePage } from "./database-connector.base.page"
import { databaseConnectorPopup } from "./database-connector.popup.page"

class DatabaseConnectorPage extends DatabaseConnectorBasePage {

  elements = {
    newConnection: ".connection-containerMain button",
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

  verifyConnectionExists(host: string, port: string, id: string) {
    this.getElementInIframe(this.elements.connectionId).parent().should('contain', id);
    this.getElementInIframe(this.elements.host).parent().should('contain', host);
    this.getElementInIframe(this.elements.port).parent().should('contain', port);
  }


  /**
   * delete all connections
   */
  cleanUp() {
    this.getElementInIframe("md-card").each(card => {
      //open context menu and click on delete
        card.find("button").click()
        this.contextMenu("delete")

        //check delete was successful
        super.getIframeElement("database-connector", "md-toast").should('contain', 'Successfully deleted connection!')
    })
  }

//---------------------------
  /**
   * context menu of an existing connection
   * @param action - one of the options in the menu
   */
  contextMenu(action: string) {
    switch (action) {
      case 'delete':
        this.getElementInIframe("md-menu-content")
            .find("[message-key='dc_databaseConnector.label.delete']")
            .click({timeout: 1000})

        this.getElementInIframe("[message-key='dc_databaseConnector.label.delete']")
            .should("not.be.visible")
        this.deleteDialog(true)
        break
      case 'disconnect':
        //
            break
      case 'edit':
        //
            break
      case 'export':
        //
            break
      case 'connection statistics':
        //
            break
      default:
        console.error("the action: " + action + " does not exist in the context menu")
            break
    }
  }

  deleteDialog(confirmed: boolean) {
    if (confirmed) {
      this.getElementInIframe(".md-dialog-content").find("[message-key='dc_databaseConnector.label.delete']").click({timeout: 2000})
    } else {
      this.getElementInIframe(".md-dialog-content").find("[message-key='dc_databaseConnector.label.cancel']").click({timeout: 2000})
    }
  }

}
export const databaseConnector = new DatabaseConnectorPage()
