Cypress.Commands.add('getByTestId', testId => cy.get(`[data-testid="${testId}"]`));

Cypress.Commands.add('interceptApi', (method, path, alias) => {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;

  return cy.env<{ apiUrl: string }>(['apiUrl']).then(({ apiUrl }) => {
    if (!apiUrl) {
      throw new Error('L’URL de l’API E2E est absente de la configuration Cypress.');
    }

    return cy.intercept(method, `${apiUrl}${normalizedPath}`).as(alias);
  });
});
