import { DatabaseConnectorBasePage } from "./database-connector.base.page"
import 'cypress-file-upload'

class DatabaseConnectorImport extends DatabaseConnectorBasePage {

    elements = {
        importBtn: "button[aria-label='Import']",
        importInput: "#importFileSelector",
        notification: "md-toast"
    }

    importConnection(fileName: string) {
        // this.getElementInIframe(this.elements.importBtn).click()
        super.getIframeElement("database-connector", this.elements.importInput).attachFile("../resources/files/" + fileName, { force: true })
        return this
    }

    verifyInvalidConnectionMessage() {
        return super.getIframeElement("database-connector", this.elements.notification).should('contain', 'Connection is invalid!')
    }

    verifyValidConnectionMessage() {
        return super.getIframeElement("database-connector", this.elements.notification).should('contain', 'All connections imported successfully!')
    }


}
export const databaseConnectorImport = new DatabaseConnectorImport()
