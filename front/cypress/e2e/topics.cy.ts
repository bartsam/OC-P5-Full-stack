describe('Topics', () => {
  it('displays all available topics', () => {
    cy.login();
    cy.interceptApi('GET', '/topics', 'topics');

    cy.visit('/topics');

    cy.wait('@topics').then(({ response }) => {
      expect(response?.statusCode).to.equal(200);
      expect(response?.body).to.have.length(6);
    });
    cy.getByTestId('topics-list').should('be.visible');
    topicCard('Java').within(() => {
      cy.getByTestId('subscribe-button').should('be.enabled');
    });
  });

  it('subscribes to a topic', () => {
    cy.login();
    cy.interceptApi('GET', '/topics', 'topics');
    cy.interceptApi('POST', '/topics/1/subscribe', 'subscribe');
    cy.interceptApi('DELETE', '/topics/1/subscribe', 'unsubscribe');

    cy.visit('/topics');
    cy.wait('@topics').its('response.statusCode').should('eq', 200);

    topicCard('Java').within(() => {
      cy.getByTestId('subscribe-button').click();
    });

    cy.wait('@subscribe').then(({ request, response }) => {
      expect(request.body).to.deep.equal({});
      expect(response?.statusCode).to.equal(204);
    });
    topicCard('Java').within(() => {
      cy.getByTestId('subscribe-button').should('be.disabled').and('contain.text', 'Déjà abonné');
    });

    cy.interceptApi('GET', '/profile', 'profile');
    cy.interceptApi('GET', '/topics/subscribed', 'subscribedTopics');
    cy.visit('/profile');
    cy.wait('@profile').its('response.statusCode').should('eq', 200);
    cy.wait('@subscribedTopics').its('response.statusCode').should('eq', 200);
    topicCard('Java').within(() => {
      cy.getByTestId('unsubscribe-button').click();
    });
    cy.wait('@unsubscribe').its('response.statusCode').should('eq', 204);
    cy.getByTestId('topics-list').should('not.contain.text', 'Java');
  });

  it('displays subscriptions on the profile and unsubscribes from a topic', () => {
    cy.login();
    cy.interceptApi('GET', '/topics', 'topics');
    cy.interceptApi('POST', '/topics/2/subscribe', 'subscribe');

    cy.visit('/topics');
    cy.wait('@topics').its('response.statusCode').should('eq', 200);
    topicCard('JavaScript').within(() => {
      cy.getByTestId('subscribe-button').click();
    });
    cy.wait('@subscribe').its('response.statusCode').should('eq', 204);

    cy.interceptApi('GET', '/profile', 'profile');
    cy.interceptApi('GET', '/topics/subscribed', 'subscribedTopics');
    cy.interceptApi('DELETE', '/topics/2/subscribe', 'unsubscribe');

    cy.visit('/profile');
    cy.wait('@profile').its('response.statusCode').should('eq', 200);
    cy.wait('@subscribedTopics').its('response.statusCode').should('eq', 200);
    topicCard('JavaScript').within(() => {
      cy.getByTestId('unsubscribe-button').click();
    });

    cy.wait('@unsubscribe').then(({ response }) => {
      expect(response?.statusCode).to.equal(204);
    });
    cy.getByTestId('topics-list').should('not.contain.text', 'JavaScript');
  });

  function topicCard(name: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.contains('[data-testid="topic-title"]', name).closest('app-topics-item');
  }
});
