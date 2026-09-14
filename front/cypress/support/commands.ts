Cypress.Commands.add('getByTestId', testId => cy.get(`[data-testid="${testId}"]`));

Cypress.Commands.add('login', (identifier = 'user-test', password = 'Password123!') => {
  const credentials = { identifier, password };

  cy.interceptApi('POST', '/auth/login', 'login');
  cy.visit('/login');
  cy.getByTestId('identifier-input').focus().type(credentials.identifier);
  cy.getByTestId('password-input').focus().type(credentials.password);
  cy.getByTestId('submit-button').click();

  cy.wait('@login').then(({ request, response }) => {
    expect(request.body).to.deep.equal(credentials);
    expect(response?.statusCode).to.equal(200);
  });
  cy.location('pathname').should('eq', '/posts/feed');
});

Cypress.Commands.add('interceptApi', (method, path, alias) => {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;

  return cy.env<{ apiUrl: string }>(['apiUrl']).then(({ apiUrl }) => {
    if (!apiUrl) {
      throw new Error('L’URL de l’API E2E est absente de la configuration Cypress.');
    }

    return cy.intercept(method, `${apiUrl}${normalizedPath}`).as(alias);
  });
});
