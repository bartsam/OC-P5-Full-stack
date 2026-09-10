import { faker } from '@faker-js/faker';

describe('Posts', () => {
  it('creates a post', () => {
    const post = newPost();

    cy.login();
    createPost(post).then(() => {
      cy.location('pathname').should('eq', '/posts/feed');
      cy.get('mat-snack-bar-container').should('contain.text', 'Article créé avec succès');
    });
  });

  it('displays the feed and toggles its sort order', () => {
    cy.interceptApi('GET', '/posts?sort=desc', 'descendingFeed');

    cy.login();
    cy.wait('@descendingFeed').its('response.statusCode').should('eq', 200);
    cy.getByTestId('posts-list').should('be.visible');

    cy.interceptApi('GET', '/posts?sort=asc', 'ascendingFeed');
    cy.getByTestId('sort-button').click();

    cy.wait('@ascendingFeed').its('response.statusCode').should('eq', 200);
    cy.getByTestId('sort-button')
      .should('contain.text', 'Trier par')
      .find('mat-icon')
      .should('have.text', 'arrow_upward_alt');
  });

  it('adds and displays a comment on a post detail page', () => {
    const post = newPost();
    const comment = faker.lorem.sentence();

    cy.login();
    createPost(post).then(postId => {
      cy.interceptApi('GET', `/posts/${postId}/comments`, 'comments');
      cy.interceptApi('POST', `/posts/${postId}/comments`, 'createComment');

      cy.contains('[data-testid="post-title"]', post.title)
        .should('be.visible')
        .closest('a')
        .click();
      cy.getByTestId('post-title').should('have.text', post.title);
      cy.wait('@comments').its('response.statusCode').should('eq', 200);

      cy.get('app-create-comment').within(() => {
        cy.getByTestId('content-input').focus().type(comment);
        cy.getByTestId('submit-button').click();
      });

      cy.wait('@createComment').then(({ request, response }) => {
        expect(request.body).to.deep.equal({ content: comment });
        expect(response?.statusCode).to.equal(201);
      });
      cy.get('mat-snack-bar-container').should('contain.text', 'Commentaire créé avec succès');

      cy.reload();
      cy.wait('@comments').its('response.statusCode').should('eq', 200);
      cy.get('app-comments-list')
        .contains('[data-testid="post-content"]', comment)
        .should('be.visible');
      cy.get('app-comments-list')
        .contains('[data-testid="post-author"]', 'user-test')
        .should('be.visible');
    });
  });

  function createPost(post: { title: string; content: string }): Cypress.Chainable<number> {
    cy.interceptApi('GET', '/topics/options', 'topicOptions');
    cy.interceptApi('POST', '/posts', 'createPost');

    cy.getByTestId('create-button').click();
    cy.wait('@topicOptions').its('response.statusCode').should('eq', 200);
    cy.getByTestId('topic-input').click();
    cy.contains('mat-option', 'Java').click();
    cy.getByTestId('title-input').focus().type(post.title);
    cy.getByTestId('content-input').focus().type(post.content);
    cy.getByTestId('submit-button').click();

    return cy.wait('@createPost').then(({ request, response }) => {
      expect(request.body).to.include(post);
      expect(request.body.topicId).to.be.a('number');
      expect(response?.statusCode).to.equal(201);

      return response?.body.id as number;
    });
  }

  function newPost(): { title: string; content: string } {
    return {
      title: `Article E2E ${faker.string.alphanumeric({ length: 12, casing: 'upper' })}`,
      content: faker.lorem.paragraph(),
    };
  }
});
