import { Injectable } from "@angular/core";
import {
  Board,
  Component,
  Container,
  Image,
  ImageCreate,
  Text,
  Note,
  Task,
  Label,
  CalendarCreate,
  Calendar,
  Video,
  VideoCreate,
} from "../dtos/component";
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
    console.log("Create Board: ", board);
    return this.httpClient.post<Board>(this.componentBaseUri + "/board", board);
  }

  createText(component: Text): Observable<Text> {
    console.log("Create Text Component: ", component);
    return this.httpClient.post<Text>(this.componentBaseUri + "/text", component);
  }

  updateText(component: Partial<Text>): Observable<Text> {
    console.log("Updated Text Component: ", component);
    return this.httpClient.patch<Text>(this.componentBaseUri + "/text", component);
  }

  createTask(task: Task): Observable<Task> {
    return this.httpClient.post<Task>(this.componentBaseUri + "/task", task);
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

  createVideo(video: VideoCreate, data?: Blob) {
    console.log("Create Video: ", video);

    const form = new FormData();
    form.append("component", new Blob([JSON.stringify(video)], { type: "application/json" }));
    if (data) form.append("video", data);
    return this.httpClient.post<Video>(this.componentBaseUri + "/video", form);
  }

  getVideoContent(video: Video) {
    return this.httpClient.get(this.componentBaseUri + "/video/" + video.id, { responseType: "blob" });
  }

  setVideoContent(video: Video, data: Blob) {
    const form = new FormData();
    form.append("component", new Blob([JSON.stringify(video)], { type: "application/json" }));
    form.append("video", data);
    return this.httpClient.put<Video>(this.componentBaseUri + "/video", form);
  }

  createCalendar(calendar: CalendarCreate) {
    console.log("Create calendar: ", calendar);
    return this.httpClient.post<Calendar>(this.componentBaseUri + "/calendar", calendar);
  }

  getCalendarContent(calendar: Calendar) {
    return this.httpClient.get<Calendar>(this.componentBaseUri + "/calendar/" + calendar.id);
  }

  setCalendarUrl(calendar: Calendar, url: string) {
    return this.httpClient.put<Calendar>(this.componentBaseUri + "/calendar/url/" + calendar.id, { url });
  }

  refreshCalendar(calendar: Calendar) {
    return this.httpClient.put<Calendar>(this.componentBaseUri + "/calendar/refresh/" + calendar.id, {});
  }

  clearCalendar(calendar: Calendar) {
    return this.httpClient.put<Calendar>(this.componentBaseUri + "/calendar/clear/" + calendar.id, {});
  }

  setCalendarFile(calendar: Calendar, file: Blob) {
    const form = new FormData();
    form.append("file", new Blob([file], { type: "text/calendar" }), "event.ics");
    return this.httpClient.put<Calendar>(this.componentBaseUri + "/calendar/file/" + calendar.id, form);
  }

  createNote(note: Partial<Note>) {
    console.log("Create note component: ", note);
    return this.httpClient.post<Text>(this.componentBaseUri + "/note", note);
  }

  updateNote(note: Partial<Note> & { id: number }) {
    console.log("Update note component: ", note);
    return this.httpClient.patch<Text>(this.componentBaseUri + "/note", note);
  }

  updatePosAndSize<T extends Component>(comp: T): Observable<T> {
    const { column, height, width, row, id, parentId } = comp;
    return this.httpClient.patch<T>(this.componentBaseUri, { column, height, width, row, id, parentId });
  }

  updateTask(task: Partial<Task>) {
    return this.httpClient.put<Component>(this.componentBaseUri + "/task", task);
  }

  repeatTask(task: Container): Observable<Task> {
    console.log("Repeat Task: " + task.id);
    return this.httpClient.put<Task>(this.componentBaseUri + "/task/repeat", task);
  }

  updateBoard(board: Partial<Board>): Observable<Board> {
    console.log("Update Board: ", board);
    return this.httpClient.patch<Board>(this.componentBaseUri + "/board", board);
  }

  changeBoardDepth(id: number, depth: number) {
    console.log("Update depth of board: ", id, " with depth ", depth);
    return this.httpClient.patch<Board>(this.componentBaseUri + "/board", { id, depth });
  }

  // TODO: maybe not needed
  getComponent<T extends Component>(componentId: number): Observable<T> {
    return this.httpClient.get<T>(this.componentBaseUri + "/" + componentId);
  }

  getRoots(): Observable<Component[]> {
    return this.httpClient.get<Component[]>(this.componentBaseUri);
  }

  deleteComponent(componentId: number): Observable<boolean> {
    return this.httpClient.delete<boolean>(this.componentBaseUri + "/" + componentId);
  }

  setLabel(label: Label) {
    return this.httpClient.post<Label>(this.componentBaseUri + "/label", label);
  }
}
