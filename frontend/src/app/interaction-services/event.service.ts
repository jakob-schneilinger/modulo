import { Injectable } from "@angular/core";
import { Subject } from "rxjs";
import {CalendarEntry, Component as Comp} from "../dtos/component";

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
  createTemplate$ = new Subject<{ component: Comp }>();
  calendarToTask$ = new Subject<{ entry: CalendarEntry }>();

  emitWidthChanged(component: Comp) {
    this.widthChanged$.next({ component });
  }

  emitTaskCreate(entry: CalendarEntry) {
    this.calendarToTask$.next({ entry });
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

  emitCreateTemplate(component: Comp) {
    this.createTemplate$.next({component});
  }
}
