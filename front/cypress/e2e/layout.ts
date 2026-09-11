describe('Layout', () => {
  it('redirects anonymous visitors away from protected pages', () => {
    cy.clearLocalStorage();

    cy.visit('/posts/create');

    cy.location('pathname').should('eq', '/');
    cy.getByTestId('landing').should('be.visible');
  });

  it('renders the not-found page and returns to the home page', () => {
    cy.visit('/page-that-does-not-exist');

    cy.contains('h1', '404').should('be.visible');
    cy.contains('a', 'Retour à l’accueil').click();
    cy.location('pathname').should('eq', '/');
  });

  it('opens and closes the mobile navigation menu', () => {
    cy.viewport('iphone-6');
    cy.login();

    cy.getByTestId('burger-button').click();
    cy.getByTestId('burger-button').should('have.attr', 'aria-expanded', 'true');
    cy.getByTestId('backdrop-button').should('be.visible').click();
    cy.getByTestId('burger-button').should('have.attr', 'aria-expanded', 'false');
  });
});
