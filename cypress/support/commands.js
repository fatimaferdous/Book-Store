Cypress.Commands.add('login', (username, password) => {
  cy.visit('/');
  cy.get('[data-testid="username-input"]').clear().type(username);
  cy.get('[data-testid="password-input"]').clear().type(password);
  cy.get('[data-testid="login-button"]').click();
});

Cypress.Commands.add('logout', () => {
  cy.get('[data-testid="logout-button"]').click();
});
