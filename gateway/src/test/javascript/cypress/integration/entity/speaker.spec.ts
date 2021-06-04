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

describe('Speaker e2e test', () => {
  let startingEntitiesCount = 0;

  before(() => {
    cy.window().then(win => {
      win.sessionStorage.clear();
    });

    cy.clearCookies();
    cy.intercept('GET', '/api/speakers*').as('entitiesRequest');
    cy.visit('');
    cy.login('admin', 'admin');
    cy.clickOnEntityMenuItem('speaker');
    cy.wait('@entitiesRequest').then(({ request, response }) => (startingEntitiesCount = response.body.length));
    cy.visit('/');
  });

  it('should load Speakers', () => {
    cy.intercept('GET', '/api/speakers*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('speaker');
    cy.wait('@entitiesRequest');
    cy.getEntityHeading('Speaker').should('exist');
    if (startingEntitiesCount === 0) {
      cy.get(entityTableSelector).should('not.exist');
    } else {
      cy.get(entityTableSelector).should('have.lengthOf', startingEntitiesCount);
    }
    cy.visit('/');
  });

  it('should load details Speaker page', () => {
    cy.intercept('GET', '/api/speakers*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('speaker');
    cy.wait('@entitiesRequest');
    if (startingEntitiesCount > 0) {
      cy.get(entityDetailsButtonSelector).first().click({ force: true });
      cy.getEntityDetailsHeading('speaker');
      cy.get(entityDetailsBackButtonSelector).should('exist');
    }
    cy.visit('/');
  });

  it('should load create Speaker page', () => {
    cy.intercept('GET', '/api/speakers*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('speaker');
    cy.wait('@entitiesRequest');
    cy.get(entityCreateButtonSelector).click({ force: true });
    cy.getEntityCreateUpdateHeading('Speaker');
    cy.get(entityCreateSaveButtonSelector).should('exist');
    cy.visit('/');
  });

  it('should load edit Speaker page', () => {
    cy.intercept('GET', '/api/speakers*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('speaker');
    cy.wait('@entitiesRequest');
    if (startingEntitiesCount > 0) {
      cy.get(entityEditButtonSelector).first().click({ force: true });
      cy.getEntityCreateUpdateHeading('Speaker');
      cy.get(entityCreateSaveButtonSelector).should('exist');
    }
    cy.visit('/');
  });

  it('should create an instance of Speaker', () => {
    cy.intercept('GET', '/api/speakers*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('speaker');
    cy.wait('@entitiesRequest').then(({ request, response }) => (startingEntitiesCount = response.body.length));
    cy.get(entityCreateButtonSelector).click({ force: true });
    cy.getEntityCreateUpdateHeading('Speaker');

    cy.get(`[data-cy="firstName"]`).type('Alison', { force: true }).invoke('val').should('match', new RegExp('Alison'));

    cy.get(`[data-cy="lastName"]`).type('Jacobson', { force: true }).invoke('val').should('match', new RegExp('Jacobson'));

    cy.get(`[data-cy="email"]`)
      .type('Delmer_Rosenbaum@gmail.com', { force: true })
      .invoke('val')
      .should('match', new RegExp('Delmer_Rosenbaum@gmail.com'));

    cy.get(`[data-cy="twitter"]`).type('neural', { force: true }).invoke('val').should('match', new RegExp('neural'));

    cy.get(`[data-cy="bio"]`).type('Loan plum', { force: true }).invoke('val').should('match', new RegExp('Loan plum'));

    cy.setFieldSelectToLastOfEntity('sessions');

    cy.get(entityCreateSaveButtonSelector).click({ force: true });
    cy.scrollTo('top', { ensureScrollable: false });
    cy.get(entityCreateSaveButtonSelector).should('not.exist');
    cy.intercept('GET', '/api/speakers*').as('entitiesRequestAfterCreate');
    cy.visit('/');
    cy.clickOnEntityMenuItem('speaker');
    cy.wait('@entitiesRequestAfterCreate');
    cy.get(entityTableSelector).should('have.lengthOf', startingEntitiesCount + 1);
    cy.visit('/');
  });

  it('should delete last instance of Speaker', () => {
    cy.intercept('GET', '/api/speakers*').as('entitiesRequest');
    cy.intercept('DELETE', '/api/speakers/*').as('deleteEntityRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('speaker');
    cy.wait('@entitiesRequest').then(({ request, response }) => {
      startingEntitiesCount = response.body.length;
      if (startingEntitiesCount > 0) {
        cy.get(entityTableSelector).should('have.lengthOf', startingEntitiesCount);
        cy.get(entityDeleteButtonSelector).last().click({ force: true });
        cy.getEntityDeleteDialogHeading('speaker').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click({ force: true });
        cy.wait('@deleteEntityRequest');
        cy.intercept('GET', '/api/speakers*').as('entitiesRequestAfterDelete');
        cy.visit('/');
        cy.clickOnEntityMenuItem('speaker');
        cy.wait('@entitiesRequestAfterDelete');
        cy.get(entityTableSelector).should('have.lengthOf', startingEntitiesCount - 1);
      }
      cy.visit('/');
    });
  });
});
