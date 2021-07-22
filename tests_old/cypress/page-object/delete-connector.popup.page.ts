import { DatabaseConnectorBasePage } from './database-connector.base.page'

class DeleteConnectorPopupPage extends DatabaseConnectorBasePage {
    elements = {
        confirmDeleteButton: "[ng-click*='dcp.deleteConnection']",
    }

    confirmDeleteConnection() {
        super.getElementInIframe(this.elements.confirmDeleteButton).click()
    }
}
export const deleteConnectorPopup = new DeleteConnectorPopupPage()
