import {
  Component as NgComponent,
  ElementRef,
  Input,
  Output,
  EventEmitter,
  ViewChild,
  HostListener,
  AfterViewInit,
  ViewContainerRef,
  SimpleChanges,
} from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { Component, Container } from "src/app/dtos/component";
import { EventService } from "src/app/interaction-services/event.service";
import { ResizeService } from "src/app/interaction-services/resize.service";
import { ComponentService } from "src/app/services/component.service";

@NgComponent({
  selector: "app-base-component",
  templateUrl: "./base.component.html",
  styleUrls: ["./base.component.scss"],
  standalone: false,
})
export class BaseComponent<T extends Component> implements AfterViewInit {
  constructor(
    protected router: Router,
    protected route: ActivatedRoute,
    protected elementRef: ElementRef,
    protected eventService: EventService,
    protected resizeService: ResizeService,
    protected componentService: ComponentService,
    protected viewContainer: ViewContainerRef
  ) {}

  @Input() depth!: number;
  @Input() self!: T;
  @Input() parentContainer!: Container;
  @Input() parentGridElInput!: ElementRef;
  @Input() homeGridElInput!: ElementRef;
  @Input() inEditMode: boolean;
  @Input() readonlyMode: boolean = false;

  @Output() startDraggingContainer = new EventEmitter<{
    component: Component;
    preview: ElementRef;
    event: MouseEvent | TouchEvent;
  }>();
  @Output() stopDraggingContainer = new EventEmitter<void>();

  @ViewChild("card", { static: false }) cardEl!: ElementRef;
  @ViewChild("previewCard", { static: false }) previewCardEl!: ElementRef;
  // @ViewChild("grid", { static: false }) gridEl!: ElementRef;
  @ViewChild("homeGrid", { static: false }) homeGridEl!: ElementRef;

  parent: ElementRef;

  isResizing = false;
  currentWidth: number = this.self?.width ?? 1;
  currentHeight: number = this.self?.height ?? 1;

  @HostListener("mousedown", ["$event"])
  @HostListener("touchstart", ["$event"])
  onMouseDown(event: MouseEvent | TouchEvent) {
    if (!this.inEditMode) return;
    this.resizeService.startResize(event, this.self, this.cardEl, this.previewCardEl);
  }

  @HostListener("document:mousemove", ["$event"])
  @HostListener("document:touchmove", ["$event"])
  onMouseMove(event: MouseEvent | TouchEvent) {
    if (!this.inEditMode) return;
    this.resizeService.resize(
      event,
      this.self,
      this.currentWidth,
      this.cardEl,
      this.previewCardEl,
      this.parentContainer,
      this.parentGridElInput,
      this.homeGridElInput,
      this.depth,
      (columns, rows) => {
        this.updateWidth(columns);
        this.updateHeight(rows);
      }
    );
  }

  @HostListener("document:mouseup", ["$event"])
  @HostListener("document:touchend", ["$event"])
  onMouseUp(event: MouseEvent | TouchEvent) {
    if (!this.inEditMode) return;
    this.resizeService.stopResize(
      this.self,
      this.currentWidth,
      this.currentHeight,
      this.previewCardEl,
      this.parentContainer,
      (columns, rows, changes) => {
        if (changes) {
          this.eventService.emitWidthChanged(this.self);
        }
        this.updateWidth(columns);
        this.updateHeight(rows);
      }
    );
  }

  updateWidth(columns: number) {
    this.currentWidth = columns;
    const hostEl = this.elementRef.nativeElement as HTMLElement;
    hostEl.style.gridColumn = `${this.self.column} / ${this.self.column + columns}`;
  }

  updateHeight(rows: number) {
    this.currentHeight = rows;
    const hostEl = this.elementRef.nativeElement as HTMLElement;
    hostEl.style.gridRow = `${this.self.row} / ${this.self.row + rows}`;
  }

  startContainerDrag(component: Component, event: MouseEvent | TouchEvent) {
    if (!this.inEditMode) return;
    event.preventDefault();
    this.startDraggingContainer.emit({ component, preview: this.previewCardEl, event });
  }

  stopContainerDrag() {
    this.stopDraggingContainer.emit();
  }

  deleteComponent() {
    this.eventService.emitDelete(this.self);
  }

  enableEditMode() {
    this.eventService.emitEnableEditMode();
  }

  capitalizeFirstLetter(word: string) {
    if (!word) return word;
    return word[0].toUpperCase() + word.slice(1);
  }

  ngAfterViewInit(): void {
    this.updateHeight(this.self.height);
    this.updateWidth(this.self.width);
  }

  public setData(data: Component) {
    for (const key in data) {
      if (!["id", "children", "parentId"].includes(key)) {
        this.self[key] = data[key];
      }
    }
    this.ngAfterViewInit();
  }
}
