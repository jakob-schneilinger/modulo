/// <reference types="cypress" />

import axios from "axios";

let user = {
    username : "e2e-user".concat(Date.now().toString()),
    email : Date.now().toString().concat("@somemail.com"),
    password: "12345678"
}
const createBoard = {
    type: "board",
    width: 2,
    height: 2,
    row: 1,
    column: 1,
    name: user.username
}
let rootBoard = {}
let token = ""

describe("managing", ()=>{

    before(() => {
        axios.post('http://localhost:8080/api/v1/user/register', {
            username: user.username,
            email: user.email,
            password: user.password
        }).then(r  => {
            token = r.data.token
            axios.post('http://localhost:8080/api/v1/component/board', createBoard, {headers: { Authorization: token }})
                .then(r => {
                    rootBoard = {...r.data}
                })
        });
        cy.wait(1000)
    })

    after(()=>{
        axios.delete('http://localhost:8080/api/v1/user/'.concat(user.username), {
            headers: { Authorization: token }
        })
    })

    beforeEach(()=>{
        cy.createSession(user.username, user.password)
        cy.login(user.username, user.password)
        cy.wait(1000)
        cy.visit("localhost:4200/#/component/".concat('' + rootBoard.id))
    })

    it("can create and delete a child board", ()=>{
        // create
        cy.get("[data-cy=create-sub-component]").click()
        cy.get("[data-cy=create-sub-board]").click()
        cy.get("[data-cy=child-board]").should("exist")
        // delete
        cy.get("[data-cy=manage-dropdown]").click()
        cy.get("[data-cy='context-Delete Board']").click()
        cy.get('[data-cy=home-board-delete-modal-confirm]').click()
        cy.wait(100)
        cy.get("[data-cy=child-board]").should("not.exist")
    })

    it("can create and delete a child text", ()=>{
        // create
        cy.get("[data-cy=create-sub-component]").click()
        cy.get("[data-cy=create-sub-text]").click()
        cy.get("[data-cy=child-text]").should("exist")
        // delete
        cy.get("[data-cy=manage-dropdown]").click()
        cy.get("[data-cy='context-Delete Text']").click()
        cy.get('[data-cy=home-board-delete-modal-confirm]').click()
        cy.wait(100)
        cy.get("[data-cy=child-text]").should("not.exist")
    })

    it("can create, select image file and delete a child image", ()=>{
        // create
        cy.get("[data-cy=create-sub-component]").click()
        cy.get("[data-cy=create-sub-image]").click()
        cy.get("[data-cy=child-image]").should("exist")

        // add an image
        cy.get("[data-cy=image-select-button]").click()
        cy.get('[data-cy=image-select-input]').selectFile('cypress/fixtures/cypress-tree.jpg', {force:true})
        cy.wait(1000)
        cy.get('img[data-cy=image-content]').isFixtureImage("cypress-tree.jpg")


        // delete
        cy.get("[data-cy=manage-dropdown]").click()
        cy.get("[data-cy='context-Delete Image']").click()
        cy.get('[data-cy=home-board-delete-modal-confirm]').click()
        cy.wait(100)
        cy.get("[data-cy=child-image]").should("not.exist")
    })




})
