import { Component, ElementRef, Input, OnDestroy, OnInit } from "@angular/core";
import { Globals } from "src/app/global/globals";
import { Notification, NotificationService, NotificationType } from "src/app/services/notification.service";

@Component({
  selector: "app-notifications",
  templateUrl: "./notifications.component.html",
  styleUrls: ["./notifications.component.scss"],
  standalone: false,
})
export class NotificationsComponent implements OnInit {
  closed: boolean = true;
  notifications: Notification[] = [];

  getTypeIcon(type: NotificationType) {
    const types: { [key in NotificationType]: string } = {
      error: "error bi bi-exclamation-diamond-fill",
      info: "info bi bi-info-circle-fill",
      log: "log bi bi-question-circle-fill",
      success: "success bi bi-check-circle-fill",
      warn: "warn bi bi-exclamation-triangle-fill",
    };
    return types[type];
  }

  constructor(private not: NotificationService, private globals: Globals) {
    // inject console
    const injectTo: NotificationType[] = globals.debugMode ? ["error"] : [];
    for (const type of injectTo) {
      const old = console[type];
      console[type] = (...params) => {
        old(...params);
        this.spawnNotification({ type, title: "Unhandled " + type + " (console)", message: params.join(", ") });
      };
    }

    /* const types = ["info", "log", "success", "warn", "error"];
    let i = 0;
    setInterval(() => {
      this.spawnNotification({
        title: "Test " + i++,
        type: types[i % types.length] as any,
        closeAfter: 2000,
        message: "This is a notification body...",
      });
    }, 1000); */
  }

  ngOnInit(): void {
    this.not.notification$.subscribe({
      next: (notification) => this.spawnNotification(notification),
      error: console.error,
    });
  }

  spawnNotification(notification: Notification) {
    if (!notification.type) notification.type = "log";
    this.notifications.push(notification);

    if (!notification.closeAfter) return;
    setTimeout(() => this.close(notification), notification.closeAfter);
  }

  close(notification: Notification) {
    const i = this.notifications.indexOf(notification);
    if (i < 0) return;

    this.notifications.splice(i, 1);
  }
}
