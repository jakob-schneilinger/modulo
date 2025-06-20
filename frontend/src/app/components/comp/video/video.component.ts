import { Component as NgComponent, ElementRef, OnInit, ViewChild } from "@angular/core";
import { BaseComponent } from "../base/base.component";
import { Component, Video } from "src/app/dtos/component";
import ComponentFactory from "src/app/global/ComponentFactory";
import type { ContextMenuAction } from "../context-menu/context-menu.component";

@NgComponent({
  selector: "app-video-component",
  templateUrl: "./video.component.html",
  styleUrls: ["./video.component.scss", "../base/base.component.scss"],
  standalone: false,
})
export class VideoComponent extends BaseComponent<Video> implements OnInit {
  @ViewChild("input") input: ElementRef<HTMLImageElement>;

  private video: ElementRef<HTMLImageElement>;
  @ViewChild("content") set content(v: ElementRef<HTMLImageElement>) {
    this.video = v;

    // wait for ui to completely refresh before changing next state
    setTimeout(() => {
      if (v && this.dataUrl) this.video.nativeElement.src = this.dataUrl;
    }, 0);
  }
  private dataUrl: string;

  actions: ContextMenuAction[] = [
    { label: "Change Video", action: () => this.focusFileInput() },
    { label: "Enable Edit Mode", action: () => this.enableEditMode() },
    { label: "Delete Video", action: () => this.deleteComponent() },
  ];

  inputActive: boolean = false;

  ngOnInit(): void {
    this.fetchVideo();
  }

  showVideo(v: Blob | string) {
    if (typeof v === "string") this.dataUrl = v;
    else if (typeof v === "object") this.dataUrl = window.URL.createObjectURL(v);
    else return;

    if (this.video) this.video.nativeElement.src = this.dataUrl;
  }

  fetchVideo() {
    return this.componentService.getVideoContent(this.self).subscribe({
      next: (v) => this.showVideo(v),
      error: (e) => {
        if (e.status == 404) {
          this.inputActive = true;
          return;
        }
        console.error(e);
      },
    });
  }

  setVideo(video: File) {
    if (!video) return;

    this.componentService.setVideoContent(this.self, video).subscribe({
      next: () => {
        this.inputActive = false;
        this.showVideo(video);
      },
      error: (e) => {
        this.notification.warn("Video incompatible!", "Check that your video is not bigger than 20\xa0MB and has the correct file type (mp4 or mov).", 5000)
        console.error(e)
      },
    });
  }

  focusFileInput() {
    if (this.readonlyMode) return;
    this.input.nativeElement.click();
  }

  setData(data: Component) {
    super.setData(data);
    this.fetchVideo();
  }
}
ComponentFactory.addComponentType("video", VideoComponent);
