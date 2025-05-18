import { Injectable } from "@angular/core";
import { Board, BoardCreate, Component, Container } from "../dtos/component";
import { Observable } from "rxjs";
import { HttpClient } from "@angular/common/http";
import { Globals } from "../global/globals";

@Injectable({
  providedIn: "root",
})
export class ComponentService {
  private componentBaseUri: string = this.globals.backendUri + "/component";

  constructor(private httpClient: HttpClient, private globals: Globals) {}

  createBoard(board: Board, parentId?: number) {
    console.log("Create Board: ");
    console.log(board);
    const createBoard: BoardCreate = {
      ...board,
      parentId: parentId,
    };
    return this.httpClient.post<Board>(this.componentBaseUri + "/board", createBoard);
  }

  updateContainer(container: Container): Observable<Container> {
    switch (container.type) {
      case "board":
        return this.httpClient.put<Board>(this.componentBaseUri + "/board/" + container.id, container); // TODO: maybe exchange container for specific object if needed
      case "task":
        return this.httpClient.put<Board>(this.componentBaseUri + "/task/" + container.id, container);
      case "note":
        return this.httpClient.put<Board>(this.componentBaseUri + "/note/" + container.id, container);
      default: //TODO add default case if needed ??
        return;
    }
  }

  updateBoard(board: Board): Observable<Board> {
    console.log("Update Board: ", board);
    return this.httpClient.put<Board>(this.componentBaseUri + "/board", board);
  }

  getComponent(componentId: number): Observable<Component> {
    return this.httpClient.get<Component>(this.componentBaseUri + "/" + componentId);
  }

  getRoots(): Observable<Component[]> {
    return this.httpClient.get<Component[]>(this.componentBaseUri);
  }

  deleteComponent(componentId: number): Observable<boolean> {
    return this.httpClient.delete<boolean>(this.componentBaseUri + "/" + componentId);
  }
}
