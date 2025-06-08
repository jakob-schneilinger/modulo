import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from "@angular/core";
import { BaseComponent } from "../base/base.component";
import { Image } from "src/app/dtos/component";
import { CommonModule } from "@angular/common";
import ComponentFactory from "src/app/global/ComponentFactory";
import type { ContextMenuAction } from "../context-menu/context-menu.component";

@Component({
  selector: "app-image-component",
  templateUrl: "./image.component.html",
  styleUrls: ["./image.component.scss", "../base/base.component.scss"],
  standalone: false,
})
export class ImageComponent extends BaseComponent<Image> implements OnInit, OnDestroy {
  @ViewChild("input") input: ElementRef<HTMLImageElement>;

  private img: ElementRef<HTMLImageElement>;
  @ViewChild("content") set content(v: ElementRef<HTMLImageElement>) {
    if (v) this.img = v;
  }
  private dataUrl: string;

  private _canvas: ElementRef<HTMLCanvasElement>;
  @ViewChild("canvas") set canvas(v: ElementRef<HTMLCanvasElement>) {
    if (v) this._canvas = v;
  }

  private _brushSizeInput: ElementRef<HTMLInputElement>;
  @ViewChild("canvas") set brushSizeInput(v: ElementRef<HTMLInputElement>) {
    if (v) this._brushSizeInput = v;
  }

  sketching: boolean = false;
  brushSize: number = 4;
  colors: string[] = ["black", "red", "green", "blue", "yellow", "white"];
  color: string = "black";

  actions: ContextMenuAction[] = [
    { label: "Change Image", action: () => this.focusFileInput() },
    { label: "Start Sketching", action: () => this.sketch() },
    { label: "Enable Edit Mode", action: () => this.enableEditMode() },
    { label: "Delete Image", action: () => this.deleteComponent() },
  ];

  inputActive: boolean = false;

  ngOnInit(): void {
    this.componentService.getImageContent(this.self).subscribe({
      next: (v) => {
        this.dataUrl = window.URL.createObjectURL(v);
        this.img.nativeElement.src = this.dataUrl;
      },
      error: (e) => {
        if (e.status == 404) {
          this.inputActive = true;
          return;
        }
        console.error(e);
      },
    });

    this.mouseDown = this.mouseDown.bind(this);
    this.mouseUp = this.mouseUp.bind(this);

    document.body.addEventListener("mousedown", this.mouseDown);
    document.body.addEventListener("mouseup", this.mouseUp);
  }

  ngAfterViewInit(): void {
    super.ngAfterViewInit();

    if ((this.self as any).sketch) setTimeout(() => this.sketch(), 0);
  }

  ngOnDestroy() {
    document.body.removeEventListener("mousedown", this.mouseDown);
    document.body.removeEventListener("mouseup", this.mouseUp);
  }

  setImage(image: File) {
    if (!image) return;

    this.componentService.setImageContent(this.self, image).subscribe({
      next: () => {
        this.inputActive = false;
        this.dataUrl = window.URL.createObjectURL(image);
        this.img.nativeElement.src = this.dataUrl;
      },
      error: (e) => console.error(e),
    });
  }

  focusFileInput() {
    this.input.nativeElement.click();
  }

  private ctx: CanvasRenderingContext2D;
  sketch() {
    this.sketching = true;

    setTimeout(() => {
      const canvas = this._canvas.nativeElement;
      if (this.img.nativeElement.src) {
        canvas.width = this.img.nativeElement.width;
        canvas.height = this.img.nativeElement.height;
      }

      this.ctx = canvas.getContext("2d");
      this.ctx.lineJoin = "round";
      this.ctx.lineCap = "round";

      if (this.img.nativeElement.src) this.ctx.drawImage(this.img.nativeElement, 0, 0);
    }, 0);
  }

  relMousePos(event: MouseEvent) {
    var rect = this._canvas.nativeElement.getBoundingClientRect();
    const sx = this._canvas.nativeElement.width / rect.width;
    const sy = this._canvas.nativeElement.height / rect.height;

    return {
      x: Math.round((event.clientX - rect.left) * sx),
      y: Math.round((event.clientY - rect.top) * sy),
    };
  }

  private drawing: boolean = false;
  mouseDown(ev: MouseEvent) {
    if (this.sketching) {
      this.drawing = true;

      const { x, y } = this.relMousePos(ev);
      this.ctx.beginPath();
      this.ctx.moveTo(x, y);
    }
  }

  mouseUp(ev: MouseEvent) {
    if (this.sketching) {
      this.drawing = false;
      const { x, y } = this.relMousePos(ev);
      this.ctx.closePath();

      this.ctx.beginPath();
      this.ctx.arc(x, y, this.brushSize / 2, 0, 2 * Math.PI);
      this.ctx.fill();
      this.ctx.closePath();
    }
  }

  movePencil(event: MouseEvent) {
    if (!this.ctx) {
      console.warn("Can't access canvas context");
      return;
    }

    if (this.drawing) {
      this.ctx.fillStyle = this.color;
      this.ctx.strokeStyle = this.color;
      this.ctx.lineWidth = this.brushSize;

      const { x, y } = this.relMousePos(event);
      this.ctx.lineTo(x, y);
      this.ctx.stroke();
    }
  }

  save() {
    this.sketching = false;

    this._canvas.nativeElement.toBlob((blob) => {
      this.componentService.setImageContent(this.self, blob).subscribe();
    });

    setTimeout(() => {
      this.img.nativeElement.src = this._canvas.nativeElement.toDataURL();
    }, 0);
  }

  end() {
    this.sketching = false;
    setTimeout(() => {
      this.img.nativeElement.src = this.dataUrl;
    }, 0);
  }
}
ComponentFactory.addComponentType("image", ImageComponent);
