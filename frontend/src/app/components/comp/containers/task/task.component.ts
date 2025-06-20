import { AfterViewInit, Component, ElementRef, Input, OnDestroy, OnInit, ViewChild } from "@angular/core";
import { Component as Comp, Task, isType } from "../../../../dtos/component";
import { formatDate } from "@angular/common";
import { TimerService } from "../../../../interaction-services/timer.service";
import { Subscription } from "rxjs";
import { inject } from "@angular/core";
import ComponentFactory from "src/app/global/ComponentFactory";
import { ContainerComponent } from "../container.component";
import type { ContextMenuAction } from "../../context-menu/context-menu.component";

@Component({
  selector: "app-task-component",
  templateUrl: "./task.component.html",
  styleUrls: ["./task.component.scss", "../../base/base.component.scss", "../container.component.scss"],
  standalone: false,
})
export class TaskComponent extends ContainerComponent<Task> implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild("grid", { static: false }) gridEl!: ElementRef;

  private timerService = inject(TimerService);
  private timerSub?: Subscription;

  actions: ContextMenuAction[] = [
    { label: "Edit Title", action: () => this.startEditTitle() },
    { label: "Enable Edit Mode", action: () => this.enableEditMode() },
    { label: "Create Template", action: () => this.createTemplate() },
    { label: "Delete Task", action: () => this.deleteComponent() },
  ];

  isTask = false;
  isNotChildOfTask: boolean = false;
  parent: ElementRef;
  editingStartDate = false;
  editingEndDate = false;

  editingTitle = false;
  titleBuffer = "";

  ngAfterViewInit(): void {
    super.ngAfterViewInit();
    this.handleRepeatable();
  }

  ngOnInit(): void {
  }

  onCompletedChange(checked: boolean) {
    this.self.completed = checked;
    this.save();
  }

  startEditTitle(): void {
    if (this.readonlyMode) return;
    this.titleBuffer = this.self.name;
    this.editingTitle = true;
  }

  saveTitle(): void {
    const trimmedTitle = this.titleBuffer.trim();
    const changed = this.self.name !== trimmedTitle;
    this.self.name = trimmedTitle;
    this.editingTitle = false;
    if (changed) {
      this.componentService.updateTask(this.self).subscribe();
    }
  }

  cancelTitle(): void {
    this.editingTitle = false;
  }

  save() {
    this.eventService.emitTaskChanged(this.self);
  }

  taskInProgress() {
    if (this.self) if (!this.self.completed) return !this.self.completed;
  }

  getCompletedTasks(): number {
    return this.self.children ? this.countCompletedTasks(this.self.children) : 0;
  }

  private countCompletedTasks(children: Comp[]): number {
    return children.reduce((sum, child) => {
      const nestedCompleted = (isType(child,"note") || isType(child, "board")) && child.children ? this.countCompletedTasks(child.children) : 0;
      let isCompletedTask = 0
      if(isType(child, "task")) {
        const completed = child.completed ? 1 : 0;
        isCompletedTask = child.children ? this.countCompletedTasks(child.children) + completed : completed;
      }
      return sum + isCompletedTask + nestedCompleted;
    }, 0);
  }


  getTotalTasks(): number {
    return this.self.children ? this.countTasks(this.self.children) : 0;
  }

  private countTasks(children: Comp[]): number {
    return children.reduce((sum, child) => {
      const nestedTasks = (isType(child,"note") || isType(child, "board")) && child.children ? this.countTasks(child.children) : 0;
      const isTask = isType(child, "task") ? this.countTasks(child.children) + 1  : 0;
      return sum + isTask + nestedTasks;
    }, 0);
  }

  taskOverdue() {
    if (this.isTask && this.self.endDate && !this.self.repeating) {
      const end = new Date(this.self.endDate);
      const now = new Date();
      if (
        end.getTime() < new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime() &&
        !this.self.completed
      ) {
        return true;
      }
    }
    return false;
  }

  isRepeateable():boolean{
    return !isType(this.parentContainer,"task");
  }

  private handleRepeatable() {
      if (this.self.repeating === true) {
        if (this.self.startDate) {
          const date = new Date(this.self.endDate);
          const now = new Date();
          if (date.getTime() < now.getTime()) {
            this.eventService.emitTaskRepeated(this.self);
          }
        }
      }
  }

  endDateEditable(): boolean {
    return !!this.self.endDate;
  }

  startDateEditable() {
    return !!this.self.startDate;
  }

  deleteStartDate() {
    this.self.startDate = null;
    this.editingStartDate = false;
    this.save();
  }

  deleteEndDate() {
    this.self.endDate = null;
    this.editingEndDate = false;
    this.save();
  }

  setRepeating() {
    if (this.self.repeating) {
      if(this.timerSub){
        this.timerSub.unsubscribe();
      }
      this.self.repeating = false;
      this.save();
    } else {
      if (this.self.endDate && this.self.startDate) {
        this.timerSub = this.timerService.tick$.subscribe(() => {
          this.handleRepeatable();
        });
        this.self.repeating = true;
        this.save();
      }
    }
  }

  available(): boolean {
    if (this.readonlyMode) return true;
    if (this.self.startDate) {
      const date = new Date(this.self.startDate);
      const now = new Date();
      if (date.getTime() > now.getTime()) {
        return true;
      }
    }
    if (this.getCompletedTasks() < this.getTotalTasks()) {
      return true;
    }
    return false;
  }

  formatIsoDate(date: Date): string {
    return formatDate(date, "yyyy-MM-dd", "en-DK");
  }

  public get startDateText(): string {
    if (!this.self.startDate) {
      return "";
    } else {
      return this.formatIsoDate(this.self.startDate);
    }
  }

  public set startDateText(date: string) {
    if (date == null || date === "") {
    } else {
      this.self.startDate = new Date(date);
      this.save();
    }
  }

  public get endDateText(): string {
    if (!this.self.endDate) {
      return "";
    } else {
      return this.formatIsoDate(this.self.endDate);
    }
  }

  public set endDateText(date: string) {
    if (date == null || date === "") {
    } else {
      this.self.endDate = new Date(date);
      this.save();
    }
  }

  ngOnDestroy() {
    this.timerSub?.unsubscribe();
  }
}
ComponentFactory.addComponentType("task", TaskComponent);
