import { Injectable } from "@angular/core";
import { Subject } from "rxjs";
import { Component as Comp } from "../dtos/component";

@Injectable({
  providedIn: "root",
})
export class EventService {
  widthChanged$ = new Subject<{ component: Comp }>();
  textChanged$ = new Subject<{ component: Comp }>();
  enableEditMode$ = new Subject<void>();
  deleteComponent$ = new Subject<{ component: Comp }>();
  taskChanged$ = new Subject<{ component: Comp }>();
  taskRepeated$ = new Subject<{ component: Comp }>();

  emitWidthChanged(component: Comp) {
    this.widthChanged$.next({ component });
  }

  emitTextChanged(component: Comp) {
    this.textChanged$.next({ component });
  }

  emitEnableEditMode() {
    this.enableEditMode$.next();
  }

  emitDelete(component: Comp) {
    this.deleteComponent$.next({ component });
  }

  emitTaskChanged(component: Comp) {
    this.taskChanged$.next({ component });
  }

  emitTaskRepeated(component: Comp) {
    this.taskRepeated$.next({ component });
  }
}
