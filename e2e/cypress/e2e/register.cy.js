/// <reference types="cypress" />


import axios from "axios";

let user = {
    username : "e2e-user".concat(Date.now().toString()),
    email : Date.now().toString().concat("@somemail.com"),
    password: "12345678"
}
let token = ""

describe("register on modulo", ()=>{

    before(()=>{

        axios.post('http://localhost:8080/api/v1/user/register', {
            username: user.username,
            email: user.email,
            password: user.password
        }).then(r  => {
            console.log(r)
            token = r.data.token
        });
    })

    after(()=>{
        axios.delete('http://localhost:8080/api/v1/user/'.concat(user.username), {
            headers: { Authorization: token }
        })
    })

    beforeEach(()=>{
        cy.visit("localhost:4200/")
    })

    it('should load page', ()=>{
        cy.url().should('contain', "/login")
    })

    it('login without registered user fails', () => {
        cy.visit("#/login")
        cy.get("[data-cy=login-input-password]").type("12345678")
        cy.get("[data-cy=login-input-username]").type("testuser")
        cy.get("[data-cy=login-button").click()
        cy.get("[data-cy=notification-error]").should("exist")
    });

    it('register new user', () => {

        cy.get("[data-cy=register-link]").click()
        cy.url().should('contain', "/signup")
        cy.get("[data-cy=register-username]").type(Date.now().toString())
        cy.get("[data-cy=register-email]").type(Date.now().toString().concat('@mail.com'))
        cy.get("[data-cy=register-password]").type("12345678")
        cy.get("[data-cy=register-create]").click()
        cy.url().should('not.contain', "/signup")
        cy.url().should('contain', "/user/me")
    })

})