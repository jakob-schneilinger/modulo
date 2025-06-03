import {
  Component as NgComponent,
  ElementRef,
  Input,
  OnInit,
  ViewChild,
  ViewContainerRef,
  ComponentRef,
  OnChanges,
  SimpleChanges,
} from "@angular/core";
import { Component, Container } from "../../../dtos/component";
import { BaseComponent } from "../base/base.component";
import ComponentFactory from "src/app/global/ComponentFactory";

@NgComponent({
  selector: "app-container-component",
  templateUrl: "./container.component.html",
  styleUrls: ["./container.component.scss", "../base/base.component.scss"],
  standalone: false,
})
export class ContainerComponent<T extends Container> extends BaseComponent<T> implements OnChanges {
  private _children: Component[];
  private _childrenRef: ComponentRef<BaseComponent<any>>[];

  @ViewChild("grid", { static: false }) gridEl: ElementRef;
  @ViewChild("children", { read: ViewContainerRef }) childrenContainer: ViewContainerRef;

  ngAfterViewInit() {
    super.ngAfterViewInit();
    this.loadChildren();
  }

  loadChildren() {
    if (this._childrenRef) this._childrenRef.forEach((c) => c.destroy());
    this._childrenRef = [];

    if (!this.childrenContainer) return;

    this._children = this.self.children || [];
    for (const child of this._children) {
      const COMP = ComponentFactory.get(child.type);
      if (!COMP) {
        console.error("Type: " + child.type + " not defined!");
        continue;
      }

      const comp = this.childrenContainer.createComponent<BaseComponent<any>>(COMP as any);

      comp.setInput("self", child);
      comp.setInput("depth", this.depth + 1);
      comp.setInput("parentContainer", this.self);
      comp.setInput("parentGridElInput", this.gridEl);
      if( this.depth === 0) {
        comp.setInput("homeGridElInput", this.gridEl);
      } else {
        comp.setInput("homeGridElInput", this.homeGridElInput);
      }
      comp.setInput("inEditMode", this.inEditMode);

      comp.instance.startDraggingContainer.subscribe((data) => this.startDraggingContainer.emit(data));
      comp.instance.stopDraggingContainer.subscribe(() => this.stopDraggingContainer.emit());

      this._childrenRef.push(comp);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!this._childrenRef) return;
    for (const name in changes) {
      if (name == "self") this.loadChildren(); // refresh children
      else this._childrenRef.forEach((c) => c.setInput(name, changes[name].currentValue));
    }
  }
}
