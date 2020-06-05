// load type definitions that come with Cypress module
/// <reference types="cypress" />

declare namespace Cypress {
    interface Chainable {
        /**
         * Custom command to navigate to url with default authentication
         * @example cy.goTo('/start')
        */
        goTo(value: string): Chainable<Element>
    }
}

Cypress.Commands.add('goTo', function (url: string) {
    cy.visit(url, {
        auth: {
            username: 'root',
            password: 'root'
        }
    })
})