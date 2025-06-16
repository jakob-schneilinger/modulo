import { Component, OnInit, ViewChild, ViewContainerRef } from "@angular/core";
import { ContainerComponent } from "../container.component";
import { Board } from "src/app/dtos/component";
import ComponentFactory from "src/app/global/ComponentFactory";
import type { ContextMenuAction } from "../../context-menu/context-menu.component";

@Component({
  selector: "app-board-component",
  templateUrl: "./board.component.html",
  styleUrls: ["./board.component.scss", "../container.component.scss", "../../base/base.component.scss"],
  standalone: false,
})
export class BoardComponent extends ContainerComponent<Board> {
  editingTitle = false;
  titleBuffer = "";

  actions: ContextMenuAction[] = [
    { label: "Edit Title", action: () => this.startEditTitle() },
    { label: "Enable Edit Mode", action: () => this.enableEditMode() },
    { label: "Delete Board", action: () => this.deleteComponent() },
  ];

  startEditTitle(): void {
    if (this.readonlyMode) return;

    this.titleBuffer = this.self.name;
    this.editingTitle = true;
  }

  cancelTitle(): void {
    this.editingTitle = false;
  }

  saveTitle(): void {
    const trimmedTitle = this.titleBuffer.trim();
    const changed = this.self.name !== trimmedTitle;
    this.self.name = trimmedTitle;
    this.editingTitle = false;
    if (changed) {
      this.componentService.updateBoard({ id: this.self.id, name: this.self.name }).subscribe();
    }
  }
}
ComponentFactory.addComponentType("board", BoardComponent);
