import { Injectable } from "@angular/core";
import { Subject } from "rxjs";

export type NotificationType = "info" | "log" | "success" | "warn" | "error";

export interface Notification {
  type: NotificationType;
  title: string;
  message?: string;
  closeAfter?: number;
}

@Injectable({
  providedIn: "root",
})
export class NotificationService {
  public notification$: Subject<Notification> = new Subject<Notification>();

  public info(title: string, message?: string, duration: number = 3000) {
    this.notify(title, "info", message, duration);
  }

  public log(title: string, message?: string, duration: number = 3000) {
    this.notify(title, "log", message, duration);
  }

  public success(title: string, message?: string, duration: number = 2000) {
    this.notify(title, "success", message, duration);
  }

  public warn(title: string, message?: string, duration: number = 5000) {
    this.notify(title, "warn", message, duration);
  }

  public error(title: string, message?: string, duration: number = 5000) {
    this.notify(title, "error", message, duration);
  }

  private notify(title: string, type: NotificationType, message: string, duration: number) {
    this.notification$.next({
      title,
      message,
      type,
      closeAfter: duration,
    });
  }
}
