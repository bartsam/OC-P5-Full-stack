describe('Login page', () => {
  const credentials = {
    identifier: 'user-test',
    password: 'Password123!',
  };

  beforeEach(() => {
    cy.visit('/login');
  });

  it('should validates required fields before submitting', () => {
    cy.getByTestId('submit-button').should('be.disabled');

    cy.getByTestId('identifier-input').focus().blur();
    cy.contains('mat-error', "Un email ou un nom d'utilisateur est obligatoire").should(
      'be.visible',
    );

    cy.getByTestId('password-input').focus().blur();
    cy.contains('mat-error', 'Un mot de passe est requis').should('be.visible');
  });

  it('should keeps the user on the login page when credentials are invalid', () => {
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

  it('should toggles password visibility', () => {
    cy.getByTestId('password-input').type(credentials.password);
    cy.getByTestId('password-input').should('have.attr', 'type', 'password');

    cy.getByTestId('password-button').click();
    cy.getByTestId('password-input').should('have.attr', 'type', 'text');
  });

  it('should authenticates the user and logs out', () => {
    cy.login();
    cy.window().its('localStorage').invoke('getItem', 'auth_token').should('be.a', 'string');

    cy.getByTestId('logout-button').click();

    cy.location('pathname').should('eq', '/');
    cy.window().its('localStorage').invoke('getItem', 'auth_token').should('be.null');
    cy.getByTestId('logout-button').should('not.exist');
  });

  it('should updates the username', () => {
    const originalProfile = {
      email: 'user@test.com',
      username: credentials.identifier,
      password: credentials.password,
    };
    const updatedProfile = { ...originalProfile, username: 'user-test-updated' };

    cy.interceptApi('GET', '/profile', 'profile');
    cy.interceptApi('PUT', '/profile', 'updateUser');
    cy.login();
    cy.visit('/profile');
    cy.wait('@profile').its('response.statusCode').should('eq', 200);

    cy.getByTestId('username-input').focus().clear().type(updatedProfile.username);
    cy.getByTestId('password-input').focus().type(originalProfile.password);
    cy.getByTestId('submit-button').click();

    cy.wait('@updateUser').then(({ request, response }) => {
      expect(request.body).to.deep.equal(updatedProfile);
      expect(response?.statusCode).to.equal(200);
    });
    cy.getByTestId('username-input').should('have.value', updatedProfile.username);

    cy.getByTestId('username-input').focus().clear().type(originalProfile.username);
    cy.getByTestId('password-input').focus().clear().type(originalProfile.password);
    cy.getByTestId('submit-button').click();

    cy.wait('@updateUser').then(({ request, response }) => {
      expect(request.body).to.deep.equal(originalProfile);
      expect(response?.statusCode).to.equal(200);
    });
    cy.getByTestId('username-input').should('have.value', credentials.identifier);
  });
});
