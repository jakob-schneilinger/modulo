import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output,
  ViewChild
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {Container, Text, Component as Comp} from "../../../../dtos/component";
import {ResizeService} from "../../../../interaction-services/resize.service";
import {EventService} from "../../../../interaction-services/event.service";

/**
 * A simple, self‑contained text item that can live inside a Container.
 * – Shows its text normally.
 * – On double‑click it switches to an inline editor.
 * – Emits no events yet; the Board just keeps the model object that it
 *   passes in via the `content` input.
 */
@Component({
  selector: 'app-text-component',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './text.component.html',
  styleUrls: ['./text.component.scss'],
})
export class TextComponent implements AfterViewInit{

  constructor(
    private elementRef: ElementRef,
    private resizeService: ResizeService,
    private eventService: EventService
  ) { }

  /** The actual text that the board (or another parent) owns */
  @Input() text!:Text;
  @Input() depth:number;
  @Input() parent:Container;
  @Input() parentGridElInput:ElementRef;
  @Input() homeGridElInput:ElementRef;
  @Input() inEditMode:boolean;

  /* TODO: remove if event service is used instead
  @Output() widthChanged = new EventEmitter<{ component: Comp }>();
  @Output() textChange = new EventEmitter<string>();
   */
  @Output() startDraggingContainer = new EventEmitter<{ component: Comp, event: MouseEvent }>();
  @Output() stopDraggingContainer = new EventEmitter<void>();

  @ViewChild('card', { static: false }) cardEl!: ElementRef;
  @ViewChild('previewCard', { static: false }) previewCardEl!: ElementRef;

  currentWidth: number = this.text?.width ?? 1;
  currentHeight: number = this.text?.height ?? 1;

  fontSize = 16;

  /** local UI state */
  editing = false;
  buffer  = '';

  startEdit(): void {
    this.buffer  = this.text?.text;
    this.editing = true;
  }

  save(): void {
    if(this.text) {
      this.text.text = this.buffer.trim();
    }
    this.editing = false;
    this.eventService.emitTextChanged(this.text);
  }

  cancel(): void {
    this.editing = false;
  }

  @HostListener('mousedown', ['$event'])
  onMouseDown(event: MouseEvent) {
    if (!this.inEditMode) return;
    this.resizeService.startResize(event, this.text, this.cardEl, this.previewCardEl);
  }

  @HostListener('document:mousemove', ['$event'])
  onMouseMove(event: MouseEvent) {
    if (!this.inEditMode) return;
    this.resizeService.resize(event, this.text, this.currentWidth, this.cardEl, this.previewCardEl, this.parent, this.parentGridElInput, this.homeGridElInput, this.depth, (columns, rows) => {
      this.updateWidth(columns);
      this.updateHeight(rows);
    });
  }

  @HostListener('document:mouseup', ['$event'])
  onMouseUp(event: MouseEvent) {
    if (!this.inEditMode) return;
    this.resizeService.stopResize(event, this.text, this.currentWidth, this.currentHeight, this.previewCardEl,  this.parent, (columns, rows, changes) => {
      this.updateWidth(columns);
      this.updateHeight(rows);
      if (changes) {
        /* TODO: remove if event service is used instead
        this.widthChanged.emit({ component: this.text });
         */
        this.eventService.emitWidthChanged(this.text);
      }
    });
  }

  updateWidth(columns: number) {
    this.currentWidth = columns;
    const hostEl = this.elementRef.nativeElement as HTMLElement;
    hostEl.style.gridColumn = `${this.text.column} / ${this.text.column + columns}`;
  }

  updateHeight(rows: number) {
    this.currentHeight = rows;
    const hostEl = this.elementRef.nativeElement as HTMLElement;
    hostEl.style.gridRow = `${this.text.row} / ${this.text.row + rows}`;
  }

  // TODO: implement like in container
  startContainerDrag(component: Comp, event: MouseEvent) {
    if (!this.inEditMode) return;
    event.preventDefault();
    this.startDraggingContainer.emit({ component, event });
  }

  stopContainerDrag() {
    this.stopDraggingContainer.emit();
  }

  enableEditMode() {
    this.eventService.emitEnableEditMode();
  }

  ngAfterViewInit(): void {
    this.updateHeight(this.text.height);
    this.updateWidth(this.text.width);
  }
}
