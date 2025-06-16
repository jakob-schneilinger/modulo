import { Component, ElementRef, ViewChild } from "@angular/core";
import { Text } from "../../../dtos/component";
import { BaseComponent } from "../base/base.component";
import ComponentFactory from "src/app/global/ComponentFactory";

import type { ContextMenuAction } from "../context-menu/context-menu.component";

/**
 * A simple, self‑contained text item that can live inside a Container.
 * – Shows its text normally.
 * – On double‑click it switches to an inline editor.
 * – Emits no events yet; the Board just keeps the model object that it
 *   passes in via the `content` input.
 */
@Component({
  selector: "app-text-component",
  templateUrl: "./text.component.html",
  styleUrls: ["./text.component.scss", "../base/base.component.scss"],
  standalone: false,
})
export class TextComponent extends BaseComponent<Text> {
  editing: boolean = false;
  editBuffer: string = "";

  contextMenuActions: ContextMenuAction[] = [
    { label: "Edit Text", action: () => this.edit() },
    { label: "Enable Edit Mode", action: () => this.enableEditMode() },
    { label: "Delete Text", action: () => this.deleteComponent() },
  ];

  @ViewChild("content") contentPreview: ElementRef;
  @ViewChild("input") input: ElementRef<HTMLInputElement>;

  edit() {
    if (this.readonlyMode) return;

    this.editBuffer = this.self.content;
    this.editing = true;
    setTimeout(() => {
      this.input.nativeElement.focus();
    }, 50);
  }

  save(): void {
    this.editing = false;
    this.self.content = this.editBuffer;

    this.eventService.emitTextChanged(this.self);
    this.renderMd();
  }

  deleteComponent() {
    this.eventService.emitDelete(this.self);
  }

  ngAfterViewInit(): void {
    super.ngAfterViewInit();
    this.renderMd();
  }

  renderMd() {
    let content = "Enter text here...";
    if (this.self.content) content = mdToHtml(this.self.content);
    this.contentPreview.nativeElement.innerHTML = content;
  }
}
ComponentFactory.addComponentType("text", TextComponent);

function mdToHtml(content: string) {
  content = content.replace(/</g, "&lt");
  content = content.replace(/>/g, "&gt");
  let html = "";
  const lines = content.split("\n");
  let lastIdent = 0;
  let ident = 0;
  for (const l of lines) {
    if (l.startsWith("#")) {
      let end = l.lastIndexOf("#", 5);
      html += `<h${end + 1}>${l.substring(end + 1)}</h${end + 1}>`;
      continue;
    }

    let t = l;
    let i = 0;
    while (t.includes("**")) t = t.replace("**", i++ % 2 == 0 ? "<i>" : "</i>");
    i = 0;
    while (t.includes("*")) t = t.replace("*", i++ % 2 == 0 ? "<b>" : "</b>");
    i = 0;
    while (t.includes("`")) t = t.replace("`", i++ % 2 == 0 ? "<code>" : "</code>");

    if (t.startsWith("-")) {
      if (lastIdent == 0) {
        ident++;
        html += "<ul>";
      }
      let li = t.substring(t.indexOf("-") + 1).trim();
      html += `<li>${li}</li>`;
    } else if (lastIdent > 0) {
      html += "</ul>";
      ident == 0;
    }
    lastIdent = ident;

    if (ident == 0) html += `<p>${t}</p>`;
  }
  return html;
}
