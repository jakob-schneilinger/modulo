import {
  AfterViewInit,
  Component,
  ElementRef, Input, OnDestroy,
  OnInit,
  ViewChild
} from "@angular/core";
import {
  Component as Comp,
  Container,
  isTask,
  Task,
  Text as myText,
  isText,
  Image,
  isImage
} from "../../../../dtos/component";
import {formatDate, NgClass, NgForOf, NgIf, NgStyle} from "@angular/common";
import {TextComponent} from "../../text/text.component";
import {FormsModule} from "@angular/forms";
import {BaseComponent} from "../../base/base.component";
import {TimerService} from "../../../../interaction-services/timer.service";
import {Subscription} from "rxjs";
import {inject} from "@angular/core";
import {ImageComponent} from "../../image/image.component";


@Component({
  selector: "app-task-component",
  templateUrl: "./task.component.html",
  styleUrls: ["./task.component.scss", "../../base/base.component.scss"],
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    TextComponent,
    NgStyle,
    NgClass,
    FormsModule,
    ImageComponent,
  ]
})
export class TaskComponent extends BaseComponent<Container> implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild("grid", {static: false}) gridEl!: ElementRef;

  private timerService = inject(TimerService);
  private timerSub?: Subscription;

  task: Task;
  isTask = false;
  @Input() isNotChildOfTask!: boolean;
  parent: ElementRef;
  editingStartDate = false;
  editingEndDate = false;

  editingTitle = false;
  titleBuffer = "";


  onCompletedChange(checked: boolean) {
    this.task.completed = checked;
    this.save()
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

  get imageChildren(): Image[] {
    return this.self.children.filter(isImage);
  }

  save() {
    this.eventService.emitTaskChanged(this.self);
  }

  deleteComponent() {
    this.eventService.emitDelete(this.self);
  }

  // TODO: find a better way to seperate Containers and other Components
  private isContainer(component: Comp): component is Container {
    return (
      (component.type === "board" ||
        component.type === "task" ||
        component.type === "note") &&
      Array.isArray(component.children)
    );
  }

  get containerChildren(): Container[] {
    return this.self.children.filter(this.isContainer);
  }

  // TODO: change if found better way
  get otherChildren(): Comp[] {
    return this.self.children.filter(child => !this.isContainer(child));
  }

  taskInProgress() {
    if (this.task)
      if (!this.task.completed) return !this.task.completed

  }

  getCompletedTasks(): number {
    return this.self.children ? this.countCompletedTasks(this.self.children) : 0;
  }

  private countCompletedTasks(children: Comp[]): number {
    return children.reduce((sum, child) => {
      let completed = 0;
      if (child.type === "task") {
        const task = child.type == 'task' ? child as Task : null
        completed = task.completed ? 1 : 0;
      }
      const childCompleted = child.children ? this.countCompletedTasks(child.children) : 0;
      return completed + childCompleted + sum;
    }, 0)
  }

  getTotalTasks(): number {
    return this.self.children ? this.countTasks(this.self.children) : 0;
  }

  private countTasks(children: Comp[]): number {
    return children.reduce((sum, child) => {
      const isTask = child.type == 'task' ? 1 : 0;
      const sumTasks = child.children ? this.countTasks(child.children) : 0;
      return isTask + sumTasks + sum;
    }, 0)
  }

  taskOverdue() {
    if (this.isTask && this.task.endDate && !this.task.repeating) {
      const end = new Date(this.task.endDate);
      const now = new Date();
      if (end.getTime() < new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime() && !this.task.completed) {
        return true;
      }
    }
    return false
  }

  get textChildren(): myText[] {
    return this.otherChildren.filter(isText);
  }


  protected readonly isText = isText;


  ngAfterViewInit(): void {
    this.handleRepeatable();
  }

  private handleRepeatable() {
    if (this.isNotChildOfTask) {
      if (this.task.repeating === true) {
        if (this.task.startDate) {
          const date = new Date(this.task.endDate);
          const now = new Date();
          if (date.getTime() < now.getTime()) {
            this.eventService.emitTaskRepeated(this.self);
          }
        }
      }
    }
  }


  ngOnInit(): void {
    if (this.self.type === "task") {
      this.task = this.self as Task;
      this.isTask = true;
      this.timerSub = this.timerService.tick$
        .subscribe(() => this.handleRepeatable());
    }
  }


  endDateEditable(): boolean {
    return !!this.task.endDate;
  }

  startDateEditable() {
    return !!this.task.startDate;
  }

  deleteStartDate() {
    this.task.startDate = null;
    this.editingStartDate = false;
    this.save();
  }

  deleteEndDate() {
    this.task.endDate = null;
    this.editingEndDate = false;
    this.save();
  }

  setRepeating() {
    if (this.task.repeating) {
      this.task.startDate = null;
      this.task.repeating = false;
      this.save();
    } else {
      if (!this.task.startDate) this.task.startDate = new Date();
      this.task.repeating = true;
      this.save();
    }
  }

  available(): boolean {
    if (this.task.startDate) {
      const date = new Date(this.task.startDate);
      const now = new Date();
      if (date.getTime() > now.getTime()) {
        return true;
      }
    }
    if (this.getCompletedTasks() < this.getTotalTasks()) {
      this.task.completed = false;
      return true;
    }
    return false;
  }


  formatIsoDate(date: Date): string {
    return formatDate(date, 'yyyy-MM-dd', 'en-DK');
  }

  public get startDateText(): string {
    if (!this.task.startDate) {
      return '';
    } else {
      return this.formatIsoDate(this.task.startDate);
    }
  }

  public set startDateText(date: string) {
    if (date == null || date === '') {
    } else {
      this.task.startDate = new Date(date);
      this.save();
    }
  }

  public get endDateText(): string {
    if (!this.task.endDate) {
      return '';
    } else {
      return this.formatIsoDate(this.task.endDate);
    }
  }

  public set endDateText(date: string) {
    if (date == null || date === '') {
    } else {
      this.task.endDate = new Date(date);
      this.save();
    }
  }

  ngOnDestroy() {
    this.timerSub?.unsubscribe();
  }

}
