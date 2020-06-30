import { BasePage } from './base.page'

export class DatabaseConnectorBasePage extends BasePage {
    /**
     * overwrites the default getIframeBody in the base class
     */
    getIframeBody(): Cypress.Chainable {
        return super.getIframeBody('database-connector')
    }

    /**
     * checks the checkboxes that are in database connector page.
     * @param checkboxLocator
     * @param check
     */
    checkbox(checkboxLocator: string, check: boolean): void {
        // i didn't check all the checkboxes in Jahia, so if they're all behaving the same this should move to base class or to cypress actions file
        this.getElementInIframe(checkboxLocator)
            .invoke('attr', 'aria-checked')
            .then(($checked: string) => {
                if ($checked == 'false' && check) {
                    this.getElementInIframe(checkboxLocator).click()
                } else if ($checked == 'true' && !check) {
                    this.getElementInIframe(checkboxLocator).click()
                }
            })
    }
}
