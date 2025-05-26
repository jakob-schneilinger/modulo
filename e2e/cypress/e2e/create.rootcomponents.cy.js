/// <reference types="cypress" />

import axios from "axios";

let user = {
    username : "e2e-user".concat(Date.now().toString()),
    email : Date.now().toString().concat("@somemail.com"),
    password: "12345678"
}
let token = ""
describe("create root components on modulo", ()=>{

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
        cy.createSession(user.username, user.password)
        cy.login(user.username, user.password)
    })

    it("can create new root board", ()=>{
        cy.visit("localhost:4200/#/user/me")
        cy.get("[data-cy=header-create-board]").click()
        cy.url().should("not.contain", "component")
        cy.wait(1000)
        cy.reload()
        cy.get("[data-cy=root-board-link]")
            .should("have.length.at.least", 1)
            .first().click()
        cy.url().should("contain", "component/")

    })

    it("should be able to delete root board", ()=>{
        cy.visit("localhost:4200/#/user/me")
        cy.get("[data-cy=header-create-board]").click()
        cy.wait(1000)
        cy.reload()
        cy.get("[data-cy=root-board-link]")
            .should("have.length.at.least", 1).first().click().then(()=>{
            cy.url().should("contain", "component/")

            cy.get('[data-cy=home-board-delete-modal]').click()
            const prev_url = cy.url()
            cy.get('[data-cy=home-board-delete-modal-confirm]').click()
            cy.url().should("not.eq", prev_url)
        })

    })

})