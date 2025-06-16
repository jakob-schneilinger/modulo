import { Injectable } from "@angular/core";
import { Globals } from "../global/globals";

import SockJS from "sockjs-client";
import { Client, Frame, over } from "webstomp-client";

import { AuthService } from "./auth.service";

export interface ComponentUpdate {
  rootId?: number;
  selfId: number;
  type: "changed" | "deleted";
}

@Injectable({ providedIn: "root" })
export class WebsocketService {
  private client: Client;
  private connected = false;
  private log: boolean = false;

  constructor(private globals: Globals, private authService: AuthService) {}

  async connect(): Promise<void> {
    if (this.connected) return;

    const token = this.authService.getToken();
    const socket = new SockJS(`${this.globals.socketUri}?token=${token}`, null, { transports: ["websocket"] });

    this.client = over(socket);
    this.client.debug = (...args) => {
      if (this.log) console.log(...args);
    };
    return new Promise((res, rej) => {
      this.client.connect({}, (frame: Frame) => {
        this.connected = true;
        res();

        // listen for new boards
        const name = this.authService.getLoggedInUser().username;
        this.client.subscribe("/board/new/" + name, (newBoard) => {
          // fire event for navbar
          window.dispatchEvent(new CustomEvent("board-created", { detail: newBoard }));
        });
      });
    });
  }

  private currentSubscription: string;
  subscribe(boardId: number, callback: (update: ComponentUpdate) => void) {
    if (this.currentSubscription) this.client.unsubscribe(this.currentSubscription);

    this.currentSubscription = `/board/${boardId}`;
    this.client.subscribe(`/board/${boardId}`, (message) => callback(JSON.parse(message.body)));
  }

  disconnect(): void {
    if (this.client && this.connected) {
      this.client.disconnect(() => {
        this.connected = false;
      });
    }
  }
}
