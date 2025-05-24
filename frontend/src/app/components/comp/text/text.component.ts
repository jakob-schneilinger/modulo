import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { Container, Text, Component as Comp } from "../../../dtos/component";
import { BaseComponent } from "../base/base.component";

/**
 * A simple, self‑contained text item that can live inside a Container.
 * – Shows its text normally.
 * – On double‑click it switches to an inline editor.
 * – Emits no events yet; the Board just keeps the model object that it
 *   passes in via the `content` input.
 */
@Component({
  selector: "app-text-component",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./text.component.html",
  styleUrls: ["./text.component.scss", "../base/base.component.scss"],
})
export class TextComponent extends BaseComponent<Text> {
  fontSize = 16;

  editingTitle = false;
  titleBuffer = "";

  /** local UI state */
  editing = false;
  buffer = "";

  startEdit(): void {
    this.buffer = this.self?.text;
    this.editing = true;
  }

  save(): void {
    if (this.self) {
      this.self.text = this.buffer.trim();
    }
    this.editing = false;
    this.eventService.emitTextChanged(this.self);
  }

  cancel(): void {
    this.editing = false;
  }

  deleteComponent() {
    this.eventService.emitDelete(this.self);
  }

  startEditTitle(): void {
    this.titleBuffer = this.self.name;
    this.editingTitle = true;
  }

  saveTitle(): void {
    const trimmedTitle = this.titleBuffer.trim();
    const changed = this.self.name !== trimmedTitle;
    this.self.name = trimmedTitle;
    this.editingTitle = false;
    if (changed) {
      this.eventService.emitTextChanged(this.self);
    }
  }

  cancelTitle(): void {
    this.editingTitle = false;
  }

  ngAfterViewInit(): void {
    this.fontSize = this.self.fontSize;
  }
}
