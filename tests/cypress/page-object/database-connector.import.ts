import { DatabaseConnectorBasePage } from './database-connector.base.page'
import 'cypress-file-upload'

class DatabaseConnectorImport extends DatabaseConnectorBasePage {
    elements = {
        importBtn: "button[aria-label='Import']",
        importInput: '#importFileSelector',
        performImport: '#performImport',
        notification: 'md-toast',
        importResultsHeader: "[message-key='dc_databaseConnector.label.modal.importResults']",
        importedConnContainer: 'md-list-item',
        successStatusBar: '.successStatusBar',
    }

    importConnection(fileName: string) {
        cy.get(this.elements.importInput).attachFile(fileName, { force: true })
        cy.get(this.elements.performImport).click({ force: true })
        return this
    }

    verifyInvalidConnectionMessage() {
        return cy.get(this.elements.notification).should('contain', 'Connection is invalid!')
    }

    verifyValidConnectionMessage() {
        return cy.get(this.elements.notification).should('contain', 'All connections imported successfully!')
    }

    verifyNumberOfImportedConnections(numberOfConnections: number) {
        expect(cy.get(this.elements.importResultsHeader).should('contain', 'Import Results'))
        expect(cy.get(this.elements.importedConnContainer).should('have.length', numberOfConnections))
        expect(cy.get(this.elements.successStatusBar).should('have.length', numberOfConnections))
    }
}
export const databaseConnectorImport = new DatabaseConnectorImport()
