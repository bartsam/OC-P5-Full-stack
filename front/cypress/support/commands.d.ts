declare namespace Cypress {
  interface Chainable<Subject = any> {
    /** Selects an element by its stable `data-testid` attribute. */
    getByTestId(testId: string): Chainable<JQuery<HTMLElement>>;

    /** Intercepts a request sent to the API configured for E2E. */
    interceptApi(method: HttpMethod, path: string, alias: string): Chainable<null>;

    /** Logs in with the E2E seeded user by default. */
    login(identifier?: string, password?: string): Chainable<void>;
  }
}
