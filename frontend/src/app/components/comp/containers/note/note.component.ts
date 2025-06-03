import { Component, ElementRef, ViewChild } from "@angular/core";
import { Label, Note } from "src/app/dtos/component";
import { ContainerComponent } from "../container.component";
import { invertColor } from "src/app/global/globals";
import ComponentFactory from "src/app/global/ComponentFactory";

import type { ContextMenuAction } from "../../context-menu/context-menu.component";

const labelColors = ["#b22222", "#fffacd", "#2e8b57", "#6a5acd"];

@Component({
  selector: "app-note-component",
  templateUrl: "./note.component.html",
  styleUrls: ["./note.component.scss", "../container.component.scss", "../../base/base.component.scss"],
  standalone: false,
})
export class NoteComponent extends ContainerComponent<Note> {
  @ViewChild("content") contentPreview: ElementRef<HTMLSpanElement>;

  editingTitle = false;
  titleBuffer = "";

  actions: ContextMenuAction[] = [
    { label: "Edit Title", action: () => this.startEditTitle() },
    { label: "Enable Edit Mode", action: () => this.enableEditMode() },
    { label: "Delete Board", action: () => this.deleteComponent() },
  ];

  lblBGColor(label: Label) {
    if (label.color) return label.color;
    let ndx =
      label.name
        .split("")
        .map((c) => c.charCodeAt(0))
        .reduce((a, b) => a + b) % labelColors.length;
    return labelColors[ndx];
  }

  lblColor(label: Label) {
    return invertColor(this.lblBGColor(label), true);
  }

  startEditTitle(): void {
    this.titleBuffer = this.self.title;
    this.editingTitle = true;
  }

  cancelTitle(): void {
    this.editingTitle = false;
  }

  saveTitle(): void {
    const trimmedTitle = this.self.title.trim();
    const changed = this.self.title !== trimmedTitle;
    this.self.title = trimmedTitle;
    this.editingTitle = false;
    if (changed) {
      this.componentService.updateNote({ id: this.self.id, title: this.self.title }).subscribe();
    }
  }

  removeLbl(label: Label) {
    if (!this.self.labels) return;
    const i = this.self.labels.findIndex((l) => label.name == l.name);
    if (i < 0) return;
    this.self.labels.splice(i, 1);
    this.componentService.updateNote({ id: this.self.id, labels: this.self.labels }).subscribe();
  }

  addLbl() {
    let name = prompt("Name of new label: ");
    if (!name) return;
    this.self.labels.push({ name });
    this.componentService.updateNote({ id: this.self.id, labels: this.self.labels }).subscribe();
  }
}
ComponentFactory.addComponentType("note", NoteComponent);
