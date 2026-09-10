describe('Login page', () => {
  const credentials = {
    identifier: 'user-test',
    password: 'Password123!',
  };

  beforeEach(() => {
    cy.visit('/login');
  });

  it('validates required fields before submitting', () => {
    cy.getByTestId('submit-button').should('be.disabled');

    cy.getByTestId('identifier-input').focus().blur();
    cy.contains('mat-error', "Un email ou un nom d'utilisateur est obligatoire").should(
      'be.visible',
    );

    cy.getByTestId('password-input').focus().blur();
    cy.contains('mat-error', 'Un mot de passe est requis').should('be.visible');
  });

  it('keeps the user on the login page when credentials are invalid', () => {
    cy.interceptApi('POST', '/auth/login', 'login');

    cy.getByTestId('identifier-input').focus().type('unknown-user');
    cy.getByTestId('password-input').focus().type(credentials.password);
    cy.getByTestId('submit-button').click();

    cy.wait('@login').its('response.statusCode').should('eq', 401);
    cy.get('mat-snack-bar-container')
      .should('be.visible')
      .and('contain.text', 'Impossible de se connecter');
    cy.location('pathname').should('eq', '/login');
    cy.window().its('localStorage').invoke('getItem', 'auth_token').should('be.null');
  });

  it('toggles password visibility', () => {
    cy.getByTestId('password-input').type(credentials.password);
    cy.getByTestId('password-input').should('have.attr', 'type', 'password');

    cy.getByTestId('password-button').click();
    cy.getByTestId('password-input').should('have.attr', 'type', 'text');
  });

  it('authenticates the user with valid credentials', () => {
    cy.interceptApi('POST', '/auth/login', 'login');

    cy.getByTestId('identifier-input').focus().type(credentials.identifier);
    cy.getByTestId('password-input').focus().type(credentials.password);
    cy.getByTestId('submit-button').click();

    cy.wait('@login').then(({ request, response }) => {
      expect(request.body).to.deep.equal(credentials);
      expect(response?.statusCode).to.equal(200);
    });
    cy.location('pathname').should('eq', '/posts/feed');
    cy.window().its('localStorage').invoke('getItem', 'auth_token').should('be.a', 'string');
  });
});
