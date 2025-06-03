import { Component, ElementRef, OnInit, ViewChild } from "@angular/core";
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
export class ImageComponent extends BaseComponent<Image> implements OnInit {
  @ViewChild("content") img: ElementRef<HTMLImageElement>;
  @ViewChild("input") input: ElementRef<HTMLInputElement>;

  actions: ContextMenuAction[] = [
    { label: "Change Image", action: () => this.focusFileInput() },
    { label: "Enable Edit Mode", action: () => this.enableEditMode() },
    { label: "Delete Image", action: () => this.deleteComponent() },
  ];

  inputActive: boolean = false;

  ngOnInit(): void {
    this.componentService.getImageContent(this.self).subscribe({
      next: (v) => (this.img.nativeElement.src = window.URL.createObjectURL(v)),
      error: (e) => {
        if (e.status == 404) {
          this.inputActive = true;
          return;
        }
        console.error(e);
      },
    });
  }

  setImage(image: File) {
    if (!image) return;

    this.componentService.setImageContent(this.self, image).subscribe({
      next: () => {
        this.inputActive = false;
        this.img.nativeElement.src = window.URL.createObjectURL(image);
      },
      error: (e) => console.error(e),
    });
  }

  focusFileInput() {
    this.input.nativeElement.click();
  }
}
ComponentFactory.addComponentType("image", ImageComponent);
