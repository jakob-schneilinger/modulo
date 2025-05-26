declare namespace Cypress{
    interface Chainable {
        /**
         * create a session and login
         */
        createSession(username: string, pass: string);
        login(username: string, pass: string);
        isFixtureImage(fixtureImage: string)
    }
}
