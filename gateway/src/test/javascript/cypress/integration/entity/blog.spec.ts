import {
  entityTableSelector,
  entityDetailsButtonSelector,
  entityDetailsBackButtonSelector,
  entityCreateButtonSelector,
  entityCreateSaveButtonSelector,
  entityEditButtonSelector,
  entityDeleteButtonSelector,
  entityConfirmDeleteButtonSelector,
} from '../../support/entity';

describe('Blog e2e test', () => {
  let startingEntitiesCount = 0;

  before(() => {
    cy.window().then(win => {
      win.sessionStorage.clear();
    });

    cy.clearCookies();
    cy.intercept('GET', '/services/blog/api/blogs*').as('entitiesRequest');
    cy.visit('');
    cy.login('admin', 'admin');
    cy.clickOnEntityMenuItem('blog');
    cy.wait('@entitiesRequest').then(({ request, response }) => (startingEntitiesCount = response.body.length));
    cy.visit('/');
  });

  it('should load Blogs', () => {
    cy.intercept('GET', '/services/blog/api/blogs*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('blog');
    cy.wait('@entitiesRequest');
    cy.getEntityHeading('Blog').should('exist');
    if (startingEntitiesCount === 0) {
      cy.get(entityTableSelector).should('not.exist');
    } else {
      cy.get(entityTableSelector).should('have.lengthOf', startingEntitiesCount);
    }
    cy.visit('/');
  });

  it('should load details Blog page', () => {
    cy.intercept('GET', '/services/blog/api/blogs*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('blog');
    cy.wait('@entitiesRequest');
    if (startingEntitiesCount > 0) {
      cy.get(entityDetailsButtonSelector).first().click({ force: true });
      cy.getEntityDetailsHeading('blog');
      cy.get(entityDetailsBackButtonSelector).should('exist');
    }
    cy.visit('/');
  });

  it('should load create Blog page', () => {
    cy.intercept('GET', '/services/blog/api/blogs*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('blog');
    cy.wait('@entitiesRequest');
    cy.get(entityCreateButtonSelector).click({ force: true });
    cy.getEntityCreateUpdateHeading('Blog');
    cy.get(entityCreateSaveButtonSelector).should('exist');
    cy.visit('/');
  });

  it('should load edit Blog page', () => {
    cy.intercept('GET', '/services/blog/api/blogs*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('blog');
    cy.wait('@entitiesRequest');
    if (startingEntitiesCount > 0) {
      cy.get(entityEditButtonSelector).first().click({ force: true });
      cy.getEntityCreateUpdateHeading('Blog');
      cy.get(entityCreateSaveButtonSelector).should('exist');
    }
    cy.visit('/');
  });

  it('should create an instance of Blog', () => {
    cy.intercept('GET', '/services/blog/api/blogs*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('blog');
    cy.wait('@entitiesRequest').then(({ request, response }) => (startingEntitiesCount = response.body.length));
    cy.get(entityCreateButtonSelector).click({ force: true });
    cy.getEntityCreateUpdateHeading('Blog');

    cy.get(`[data-cy="id"]`)
      .type('architectures Assistant Grocery', { force: true })
      .invoke('val')
      .should('match', new RegExp('architectures Assistant Grocery'));

    cy.get(`[data-cy="title"]`)
      .type('Forward value-added', { force: true })
      .invoke('val')
      .should('match', new RegExp('Forward value-added'));

    cy.get(`[data-cy="author"]`).type('monitoring', { force: true }).invoke('val').should('match', new RegExp('monitoring'));

    cy.get(`[data-cy="post"]`)
      .type('../fake-data/blob/hipster.txt', { force: true })
      .invoke('val')
      .should('match', new RegExp('../fake-data/blob/hipster.txt'));

    cy.get(entityCreateSaveButtonSelector).click({ force: true });
    cy.scrollTo('top', { ensureScrollable: false });
    cy.get(entityCreateSaveButtonSelector).should('not.exist');
    cy.intercept('GET', '/services/blog/api/blogs*').as('entitiesRequestAfterCreate');
    cy.visit('/');
    cy.clickOnEntityMenuItem('blog');
    cy.wait('@entitiesRequestAfterCreate');
    cy.get(entityTableSelector).should('have.lengthOf', startingEntitiesCount + 1);
    cy.visit('/');
  });

  it('should delete last instance of Blog', () => {
    cy.intercept('GET', '/services/blog/api/blogs*').as('entitiesRequest');
    cy.intercept('DELETE', '/services/blog/api/blogs/*').as('deleteEntityRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('blog');
    cy.wait('@entitiesRequest').then(({ request, response }) => {
      startingEntitiesCount = response.body.length;
      if (startingEntitiesCount > 0) {
        cy.get(entityTableSelector).should('have.lengthOf', startingEntitiesCount);
        cy.get(entityDeleteButtonSelector).last().click({ force: true });
        cy.getEntityDeleteDialogHeading('blog').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click({ force: true });
        cy.wait('@deleteEntityRequest');
        cy.intercept('GET', '/services/blog/api/blogs*').as('entitiesRequestAfterDelete');
        cy.visit('/');
        cy.clickOnEntityMenuItem('blog');
        cy.wait('@entitiesRequestAfterDelete');
        cy.get(entityTableSelector).should('have.lengthOf', startingEntitiesCount - 1);
      }
      cy.visit('/');
    });
  });
});
