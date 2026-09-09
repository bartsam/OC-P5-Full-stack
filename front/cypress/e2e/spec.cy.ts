describe("Page d'accueil", () => {
  it("affiche les liens d'inscription et de connexion", () => {
    cy.visit('/');
    cy.get('[data-testid="landing"]').should('be.visible');
    cy.contains('S’inscrire').should('have.attr', 'href', '/register');
    cy.contains('Se connecter').should('have.attr', 'href', '/login');
  });
});
