import { faker } from '@faker-js/faker';

describe('Register page', () => {
  const password = 'Password123!';
  const existingCredentials = {
    username: 'user-test',
    email: 'user@test.com',
    password,
  };

  beforeEach(() => {
    cy.visit('/register');
  });

  it('validates required fields before submitting', () => {
    cy.getByTestId('submit-button').should('be.disabled');

    cy.getByTestId('username-input').focus().blur();
    cy.contains('mat-error', "Le nom d'utilisateur est obligatoire").should('be.visible');

    cy.getByTestId('email-input').focus().blur();
    cy.contains('mat-error', 'Une adresse e-mail est requis').should('be.visible');

    cy.getByTestId('password-input').focus().blur();
    cy.contains('mat-error', 'Un mot de passe est requis').should('be.visible');
  });

  it('keeps the user on the registration page when the identity already exists', () => {
    cy.interceptApi('POST', '/auth/register', 'register');

    fillRegistrationForm(existingCredentials);
    cy.getByTestId('submit-button').click();

    cy.wait('@register').then(({ request, response }) => {
      expect(request.body).to.deep.equal(existingCredentials);
      expect(response?.statusCode).to.equal(409);
    });
    cy.get('mat-snack-bar-container')
      .should('be.visible')
      .and('contain.text', "Impossible de s'enregistrer");
    cy.location('pathname').should('eq', '/register');
    cy.window().its('localStorage').invoke('getItem', 'auth_token').should('be.null');
  });

  it('registers a new user with the real API', () => {
    const credentials = newCredentials();

    cy.interceptApi('POST', '/auth/register', 'register');

    fillRegistrationForm(credentials);
    cy.getByTestId('submit-button').click();

    cy.wait('@register').then(({ request, response }) => {
      expect(request.body).to.deep.equal(credentials);
      expect(response?.statusCode).to.equal(201);
    });
    cy.location('pathname').should('eq', '/posts/feed');
    cy.window().its('localStorage').invoke('getItem', 'auth_token').should('be.a', 'string');
  });

  function fillRegistrationForm(credentials: {
    username: string;
    email: string;
    password: string;
  }): void {
    cy.getByTestId('username-input').focus().type(credentials.username);
    cy.getByTestId('email-input').focus().type(credentials.email);
    cy.getByTestId('password-input').focus().type(credentials.password);
  }

  function newCredentials(): { username: string; email: string; password: string } {
    const username = `e2e${faker.string.alphanumeric({ length: 12, casing: 'lower' })}`;

    return {
      username,
      email: `${username}@test.com`,
      password,
    };
  }
});
