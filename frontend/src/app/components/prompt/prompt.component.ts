import { Component, ElementRef, EventEmitter, Input, Output, ViewChild } from "@angular/core";

@Component({
  selector: "app-prompt",
  templateUrl: "./prompt.component.html",
  styleUrls: ["./prompt.component.scss"],
  standalone: true,
})
export class PromptComponent {
  @Input("promptText") promptText: string = "Enter a value: ";
  @Input("submitText") submitText: string = "Submit";
  @Input("cancelText") cancelText: string = "Abort";

  @ViewChild("prompt") input: ElementRef;

  @Output("finish") finish = new EventEmitter<{ value: string; aborted: boolean }>();

  submit(ev: Event) {
    ev.preventDefault();

    this.finish.emit({ aborted: false, value: this.input.nativeElement.value });
  }

  abort() {
    this.finish.emit({ aborted: true, value: null });
  }

  blur() {
    this.finish.emit({ aborted: true, value: this.input.nativeElement.value });
  }
}
