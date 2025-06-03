import { Component, Input } from "@angular/core";

export interface ContextMenuAction {
  label: string;
  action: () => void;
}

@Component({
  selector: "app-context-menu",
  templateUrl: "./context-menu.component.html",
  styleUrl: "./context-menu.component.scss",
  standalone: false,
})
export class ContextMenuComponent {
  @Input() actions: ContextMenuAction[] = [];
}
