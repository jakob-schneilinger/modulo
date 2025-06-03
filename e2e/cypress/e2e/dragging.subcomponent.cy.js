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
let subBoard = {}
let token = ""
const columnNumber = 16
const rowHeight = 50

describe("tests dragging and resizing a component", ()=>{

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
                    createBoard.parentId = rootBoard.id
                    axios.post('http://localhost:8080/api/v1/component/board', createBoard, {headers: { Authorization: token }})
                        .then(r => {
                            subBoard = {...r.data}
                        })
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
        cy.wait(1000)
    })

    it('should detect a component by position', ()=>{
        // position = row column
        cy.get("[data-cy-position='1 1']").should('exist')
    })

    it('should move a component by dragging', ()=>{
        cy.get("[data-cy-position='1 1']").should('exist')

        //enable dragging
        cy.get("[data-cy=home-edit-mode]").click()

        cy.get("[data-cy=grid]").then(([grid]) =>{
            const columnWidth = Math.round(grid.clientWidth / columnNumber)
            const xOffset = 5
            const yOffset = 4
            cy.get('[data-cy=board-manage-icon]')
                .trigger('mousedown', { which: 1 , force:true})
                .trigger('mousemove', columnWidth*xOffset, rowHeight*yOffset, {force: true})
                .trigger('mouseup', { force: true })
            cy.get("[data-cy-position='1 1']").should('not.exist')
            cy.get(`[data-cy-position='${yOffset+1} ${xOffset+1}']`).should('exist')

            cy.get('[data-cy=board-manage-icon]')
                .trigger('mousedown', { which: 1 , force:true})
                .trigger('mousemove', -columnWidth*xOffset, -rowHeight*yOffset , {force: true})
                .trigger('mouseup', { force: true })
            cy.get(`[data-cy-position='${yOffset+1} ${xOffset+1}']`).should('not.exist')
            cy.get("[data-cy-position='1 1']").should('exist')



        })

    })

    it('should not move a component by dragging in navbar', ()=>{
        cy.wait(2000)
        cy.get("[data-cy-position='1 1']").should('exist').then(()=>{
            //enable dragging
            cy.get("[data-cy=home-edit-mode]").click()

            cy.get('[data-cy=board-manage-icon]')
                .trigger('mousedown', { which: 1 , force:true})
                .trigger('mousemove', { clientX: 0, clientY: -200 , force:true})
                .trigger('mouseup', { force: true })
            // did not move
            cy.wait(1000)
            cy.get("[data-cy-position='1 1']")
                .should('exist')
                .should('have.attr', 'data-cy', 'child-board')
        })

    })

    it('should not move a component by dragging in sidebar', ()=>{
        cy.wait(2000)
        cy.get("[data-cy-position='1 1']").should('exist').then(()=>{
            //enable dragging
            cy.get("[data-cy=home-edit-mode]").click()

            cy.get('[data-cy=board-manage-icon]')
                .trigger('mousedown', { which: 1 , force:true})
                .trigger('mousemove', { clientX: -300, clientY: 0 , force:true})
                .trigger('mouseup', { force: true })
            // did not move
            cy.wait(1000)
            cy.get("[data-cy-position='1 1']")
                .should('exist')
                .should('have.attr', 'data-cy', 'child-board')
        })
    })

    it('should resize a component', ()=>{
        cy.wait(2000)
        cy.get("[data-cy=grid]").should('exist').then(([grid]) => {
            const columnWidth = Math.round(grid.clientWidth / columnNumber)
            const xOffset = 5
            const yOffset = 4

            //enable dragging
            cy.get("[data-cy=home-edit-mode]").click()

            cy.get('[data-cy=board-resizer]')
                .trigger('mousedown', { which: 1 , force:true})
                .trigger('mousemove', columnWidth*xOffset, rowHeight*yOffset, {force: true})
                .trigger('mouseup', { force: true })
            // did not move and resized
            cy.wait(1000)
            cy.get("[data-cy-position='1 1']")
                .should('exist').should('have.attr', 'data-cy-size', `${createBoard.width + xOffset} ${createBoard.height + yOffset}`)

            cy.get('[data-cy=board-resizer]')
                .trigger('mousedown', { which: 1 , force:true})
                .trigger('mousemove', -columnWidth*xOffset, -rowHeight*(yOffset), {force: true})
                .trigger('mouseup', { force: true })
            // did not move and resized back
            cy.wait(1000)
            cy.get("[data-cy-position='1 1']")
                .should('exist').should('have.attr', 'data-cy-size', `${createBoard.width} ${createBoard.height}`)

        })



    })



})