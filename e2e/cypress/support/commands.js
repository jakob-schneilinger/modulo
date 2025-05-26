
Cypress.Commands.add('createSession', (username, password) => {
    cy.session([username, password], () => {
        cy.visit('localhost:4200/#/login')
        cy.get("[data-cy=login-input-username]").type(username)
        cy.get("[data-cy=login-input-password]").type(password)
        cy.get("[data-cy=login-button]").click()
    })
})
Cypress.Commands.add('login', (username, password) => {
    cy.visit('localhost:4200/#/login')
    cy.get("[data-cy=login-input-username]").type(username)
    cy.get("[data-cy=login-input-password]").type(password)
    cy.get("[data-cy=login-button").click()
})
Cypress.Commands.add(
    "isFixtureImage",
    {
        prevSubject: true
    },
    (subject, fixtureImage) => {
        cy.wrap(subject)
            .should(([img]) => {
                expect(img.complete).to.be.true;
            })
            .then(([img]) => {
                cy.fixture(fixtureImage).then(content => {
                    let fixtureImage = new Image();
                    fixtureImage.src = `data:image/jpeg;base64,${content}`;
                    return new Promise(resolve => {
                        fixtureImage.onload = () => {
                            expect(img.naturalWidth).to.equal(fixtureImage.naturalWidth);
                            expect(img.naturalHeight).to.equal(fixtureImage.naturalHeight);
                            resolve();
                        };
                    });
                });
            });
    }
);

