import { Injectable } from "@angular/core";
import { Board, Component, Container, Image, ImageCreate, Text, Task } from "../dtos/component";
import { Observable } from "rxjs";
import { HttpClient } from "@angular/common/http";
import { Globals } from "../global/globals";
import { User } from "../dtos/user";

@Injectable({
  providedIn: "root",
})
export class ComponentService {
  private componentBaseUri: string = this.globals.backendUri + "/component";

  constructor(private httpClient: HttpClient, private globals: Globals) {}

  createBoard(board: Board): Observable<Board> {
    console.log("Create Board: ", board);
    return this.httpClient.post<Board>(this.componentBaseUri + "/board", board);
  }

  createText(component: Text): Observable<Text> {
    console.log("Create Text Component: ", component);
    return this.httpClient.post<Text>(this.componentBaseUri + "/text", component);
  }

  updateText(component: Text): Observable<Text> {
    console.log("Updated Text Component: ", component);
    return this.httpClient.put<Text>(this.componentBaseUri + "/text", component);
  }

  createTask(task: Task): Observable<Task>{
    return this.httpClient.post<Task>(this.componentBaseUri + "/task", task)
  }

  createImage(image: ImageCreate, data?: Blob) {
    console.log("Create Image: ", image);

    const form = new FormData();
    form.append("component", new Blob([JSON.stringify(image)], { type: "application/json" }));
    if (data) form.append("image", data);
    return this.httpClient.post<Image>(this.componentBaseUri + "/image", form);
  }

  getImageContent(image: Image) {
    return this.httpClient.get(this.componentBaseUri + "/image/" + image.id, { responseType: "blob" });
  }

  setImageContent(image: Image, data: Blob) {
    const form = new FormData();
    form.append("component", new Blob([JSON.stringify(image)], { type: "application/json" }));
    form.append("image", data);
    return this.httpClient.put<Image>(this.componentBaseUri + "/image", form);
  }

  updatePosAndSize<T extends Component>(comp: T): Observable<T> {
    // TODO: implement a generic pos component update
    switch (comp.type) {
      case "board":
        return this.httpClient.put<T>(this.componentBaseUri + "/board", comp); // TODO: maybe exchange container for specific object if needed
      case "task":
        return this.httpClient.put<T>(this.componentBaseUri + "/task", comp);
      case "note":
        return this.httpClient.put<T>(this.componentBaseUri + "/note", comp);
      case "image":
        const form = new FormData();
        form.append("component", new Blob([JSON.stringify(comp)], { type: "application/json" }));
        return this.httpClient.put<T>(this.componentBaseUri + "/image", form);
      case "text":
        return this.httpClient.put<T>(this.componentBaseUri + "/text", comp);
      default: //TODO add default case if needed ??
        return;
    }
  }

  updateComponent(component: Component): Observable<Component> {
    // used to update with/height/row/col
    return this.httpClient.put<Component>(this.componentBaseUri + "/" + component.type, component);
  }

  repeatTask(task: Container): Observable<Task> {
    console.log("Repeat Task: " + task.id)
    return this.httpClient.put<Task>(this.componentBaseUri + "/task/repeat", task)
  }

  updateBoard(board: Board): Observable<Board> {
    console.log("Update Board: ", board);
    return this.httpClient.put<Board>(this.componentBaseUri + "/board", board);
  }

  // TODO: maybe not needed
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
