import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output,
  ViewChild
} from "@angular/core";
import {Board, Task, Note, Container, Component as Comp, isText, Text as myText} from "../../../dtos/component";
import { ActivatedRoute, Router } from "@angular/router";
import {NgClass, NgForOf, NgIf, NgStyle} from "@angular/common";
import {TextComponent} from "./text/text.component";
import { ResizeService } from '../../../interaction-services/resize.service';


@Component({
  selector: "app-container-component",
  templateUrl: "./container.component.html",
  styleUrls: ["./container.component.scss"],
  imports: [
    NgForOf,
    NgStyle,
    NgClass,
    NgIf,
    TextComponent
  ],
  standalone: true
})
export class ContainerComponent implements AfterViewInit {

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private elementRef: ElementRef,
    private resizeService: ResizeService
  ) { }

  @Input() depth!: number;
  @Input() container!: Container;
  @Input() parentContainer!: Container;
  @Input() parentGridElInput!: ElementRef;
  @Input() homeGridElInput!: ElementRef;
  @Input() inEditMode:boolean;

  @Output() widthChanged = new EventEmitter<{ component: Comp }>();
  @Output() startDraggingContainer = new EventEmitter<{ component: Comp, event: MouseEvent }>();
  @Output() stopDraggingContainer = new EventEmitter<void>();

  @ViewChild('card', { static: false }) cardEl!: ElementRef;
  @ViewChild('previewCard', { static: false }) previewCardEl!: ElementRef;
  @ViewChild('grid', { static: false }) gridEl!: ElementRef;
  @ViewChild('homeGrid', { static: false }) homeGridEl!: ElementRef;

  parent: ElementRef;

  isResizing = false;
  currentWidth: number = this.container?.width ?? 1;
  currentHeight: number = this.container?.height ?? 1;


  @HostListener('mousedown', ['$event'])
  onMouseDown(event: MouseEvent) {
    if (!this.inEditMode) return;
    this.resizeService.startResize(event, this.container, this.cardEl, this.previewCardEl);
  }

  @HostListener('document:mousemove', ['$event'])
  onMouseMove(event: MouseEvent) {
    if (!this.inEditMode) return;
    this.resizeService.resize(event, this.container, this.currentWidth, this.cardEl, this.previewCardEl, this.parentGridElInput, this.homeGridElInput, this.depth, (columns, rows) => {
      this.updateWidth(columns);
      this.updateHeight(rows);
    });
  }

  @HostListener('document:mouseup', ['$event'])
  onMouseUp(event: MouseEvent) {
    if (!this.inEditMode) return;
    this.resizeService.stopResize(event, this.container, this.currentWidth, this.currentHeight, this.previewCardEl, this.parentContainer, (columns, rows) => {
      this.updateWidth(columns);
      this.updateHeight(rows);
      if (columns !== this.container.width || rows !== this.container.height) {
        this.widthChanged.emit({ component: this.container });
      }
    });
  }

  updateWidth(columns: number) {
    this.currentWidth = columns;
    const hostEl = this.elementRef.nativeElement as HTMLElement;
    hostEl.style.gridColumn = `${this.container.column} / ${this.container.column + columns}`;
  }

  updateHeight(rows: number) {
    this.currentHeight = rows;
    const hostEl = this.elementRef.nativeElement as HTMLElement;
    hostEl.style.gridRow = `${this.container.row} / ${this.container.row + rows}`;
  }

  startContainerDrag(component: Comp, event: MouseEvent) {
    if (!this.inEditMode) return;
    event.preventDefault();
    this.startDraggingContainer.emit({ component, event });
  }

  stopContainerDrag() {
    this.stopDraggingContainer.emit();
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
    return this.container.children.filter(this.isContainer);
  }

  // TODO: change if found better way
  get otherChildren(): Comp[] {
    return this.container.children.filter(child => !this.isContainer(child));
  }

  get textChildren(): myText[] {
    return this.otherChildren.filter(isText);
  }

  asText(comp: Comp): myText {
    if(isText(comp)) {
      return comp as myText;
    }
  }

  capitalizeFirstLetter(word: string) {
    if (!word) return word;
    return word[0].toUpperCase() + word.slice(1);
  }

  protected readonly isText = isText;

  ngAfterViewInit(): void {
    this.updateHeight(this.container.height);
    this.updateWidth(this.container.width);
  }
}
