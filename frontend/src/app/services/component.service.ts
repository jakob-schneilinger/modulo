import { Injectable } from "@angular/core";
import {Board, Component, Container} from "../dtos/component";
import { Observable } from "rxjs";
import { HttpClient } from "@angular/common/http";
import { Globals } from "../global/globals";
import {User} from "../dtos/user";

@Injectable({
  providedIn: "root",
})
export class ComponentService {
  private componentBaseUri: string = this.globals.backendUri + "/component";

  constructor(private httpClient: HttpClient, private globals: Globals) {}

  createBoard(board: Board): Observable<Board>{
    //TODO: clean up
    console.log("Create Board: ")

    console.log(board)
    return this.httpClient.post<Board>(this.componentBaseUri + "/board", board);
  }

  updateContainer(container: Container): Observable<Container> {
    switch (container.type) {
      case "board":
        return this.httpClient.put<Board>(this.componentBaseUri + "/board", container);  // TODO: maybe exchange container for specific object if needed
      case "task":
        return this.httpClient.put<Board>(this.componentBaseUri + "/task", container);
      case "note":
        return this.httpClient.put<Board>(this.componentBaseUri + "/note", container);
      default : //TODO add default case if needed ??
        return
    }
  }

  updateBoard(board: Board): Observable<Board> {
    console.log("Update Board: " +board)
    return this.httpClient.put<Board>(this.componentBaseUri + "/board" , board)
  }

  // TODO: maybe not needed
  getComponent(componentId: number): Observable<Component> {
    return this.httpClient.get<Component>(this.componentBaseUri + "/" + componentId)
  }

  getRootBoards(): Observable<Board[]> {
    return this.httpClient.get<Board[]>(this.componentBaseUri)
  }

  deleteComponent(componentId: number): Observable<boolean> {
    return this.httpClient.delete<boolean>(this.componentBaseUri + "/" + componentId);
  }
}
