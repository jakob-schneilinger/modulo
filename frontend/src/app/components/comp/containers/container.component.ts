import { AfterViewInit, Component, ElementRef, ViewChild } from "@angular/core";
import {
  Container,
  Component as Comp,
  isText,
  Text as myText,
  Image,
  isImage,
  Task,
  isTask
} from "../../../dtos/component";
import { NgClass, NgForOf, NgIf, NgStyle } from "@angular/common";
import { TextComponent } from "../text/text.component";
import { FormsModule } from "@angular/forms";
import { BaseComponent } from "../base/base.component";
import { ImageComponent } from "../image/image.component";
import {TaskComponent} from "./task/task.component";

@Component({
  selector: "app-container-component",
  templateUrl: "./container.component.html",
  styleUrls: ["./container.component.scss", "../base/base.component.scss"],
  imports: [NgForOf, NgStyle, NgClass, NgIf, TextComponent, ImageComponent, FormsModule, TaskComponent],
  standalone: true,
})
export class ContainerComponent extends BaseComponent<Container> {
  /* TODO: remove if event service is used instead
  @Output() widthChanged = new EventEmitter<{ component: Comp }>();
  @Output() titleChanged = new EventEmitter<{ component: Comp }>();
   */

  @ViewChild("grid", { static: false }) gridEl!: ElementRef;

  editingTitle = false;
  titleBuffer = "";

  // TODO: find a better way to seperate Containers and other Components
  private isContainer(component: Comp): component is Container {
    return (
      (component.type === "board" || component.type === "note") &&
      Array.isArray(component.children)
    );
  }

  asTask(comp: Comp): Task {
    if(isTask(comp)) {
      return comp as Task;
    }
  }

  get containerChildren(): Container[] {
    return this.self.children.filter(this.isContainer);
  }

  get textChildren(): myText[] {
    return this.self.children.filter(isText);
  }

  get imageChildren(): Image[] {
    return this.self.children.filter(isImage);
  }

  get taskChildren(): Task[] {
    return this.self.children.filter(isTask);
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
      this.eventService.emitTitleChanged(this.self);
    }
  }

  cancelTitle(): void {
    this.editingTitle = false;
  }

  protected readonly isText = isText;
}
