/// <reference types="cypress" />

import axios from "axios";

let user = {
    username : "e2e-user".concat(Date.now().toString()),
    email : Date.now().toString().concat("@somemail.com"),
    password: "12345678"
}
let token = ""
const newPassword = "ababahalamaha"
const newEmail = "newmail@somemail.com"
describe("create root components on modulo", ()=> {

    before(() => {

        axios.post('http://localhost:8080/api/v1/user/register', {
            username: user.username,
            email: user.email,
            password: user.password
        }).then(r => {
            console.log(r)
            token = r.data.token
        });
    })

    after(() => {
        axios.delete('http://localhost:8080/api/v1/user/'.concat(user.username), {
            headers: {Authorization: token}
        })
    })

    beforeEach(() => {
        cy.createSession(user.username, user.password)
        cy.login(user.username, user.password)
        cy.wait(300)
        cy.visit("localhost:4200/#/user/me")
        cy.wait(1000)
    })

    //change display name
    it('should change name', ()=>{

        cy.get("[data-cy=user-displayname-input]").clear().type("new display name")
        cy.get("[data-cy=user-save]").click()
        cy.get("[data-cy=user-displayname]")
            .should('not.contain', user.username)
            .should('contain', "new display name")
    })
    //change email
    it('should change email', ()=>{

        cy.get("[data-cy=user-email-input]").clear().type(newEmail)
        cy.get("[data-cy=user-save]").click()
        cy.reload()

        cy.get("[data-cy=user-email-input]")
            .should('not.have.value', user.email)
            .should('have.value', newEmail)
    })
    //change password
    it('should change password', ()=>{

        cy.get("[data-cy=user-request-password-change]").click()

        cy.get("[data-cy=user-password-input-1]").should('exist').type(newPassword)
        cy.get("[data-cy=user-password-input-2]").should('exist').type(newPassword)

        cy.get("[data-cy=user-save]").click()
        //to check if it is saved - try to login with new password
        cy.login(user.username, newPassword)
        cy.url().should('not.contain', "/login")
        cy.get("[data-cy=auth-error]").should("not.exist")
        user.password = newPassword
    })

})