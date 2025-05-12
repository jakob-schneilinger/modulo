import { Injectable } from "@angular/core";
import { Board, Item } from "../dtos/items";
import { Observable } from "rxjs";
import { HttpClient } from "@angular/common/http";
import { Globals } from "../global/globals";

@Injectable({
  providedIn: "root",
})
export class ComponentService {
  private componentBaseUri: string = this.globals.backendUri + "/component";

  constructor(private httpClient: HttpClient, private globals: Globals) {}

  createBoard(board: Board): Observable<Board> {
    return this.httpClient.post<Board>(this.componentBaseUri + "/board", board)
  }

  updateBoard(board: Board): Observable<Board> {
    return this.httpClient.put<Board>(this.componentBaseUri + "/board/" + board.id, board)
  }

  // TODO: maybe not needed
  getComponent(componentId: number): Observable<Item> {
    return this.httpClient.get<Item>(this.componentBaseUri + "/" + componentId)
  }

  getMain(): Observable<Item> {
    return this.httpClient.get<Item>(this.componentBaseUri + "/")
  }

  deleteComponent(componentId: number): Observable<boolean> {
    return this.httpClient.delete<boolean>(this.componentBaseUri + "/" + componentId);
  }
}
