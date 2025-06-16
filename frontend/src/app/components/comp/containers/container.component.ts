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
import { Component, Container, isContainer } from "../../../dtos/component";
import { BaseComponent } from "../base/base.component";
import ComponentFactory from "src/app/global/ComponentFactory";

@NgComponent({
  selector: "app-container-component",
  templateUrl: "./container.component.html",
  styleUrls: ["./container.component.scss", "../base/base.component.scss"],
  standalone: false,
})
export class ContainerComponent<T extends Container> extends BaseComponent<T> implements OnChanges {
  //private _children: Component[];
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
    //this._children = [];

    if (!this.childrenContainer) return;

    for (const child of this.self.children || []) this.spawnChild(child);
  }

  spawnChild(child: Component) {
    const COMP = ComponentFactory.get(child.type);
    if (!COMP) {
      console.error("Type: " + child.type + " not defined!");
      return;
    }

    const comp = this.childrenContainer.createComponent<BaseComponent<any>>(COMP as any);

    comp.setInput("self", child);
    comp.setInput("depth", this.depth + 1);
    comp.setInput("parentContainer", this.self);
    comp.setInput("parentGridElInput", this.gridEl);
    if (this.depth === 0) {
      comp.setInput("homeGridElInput", this.gridEl);
    } else {
      comp.setInput("homeGridElInput", this.homeGridElInput);
    }
    comp.setInput("inEditMode", this.inEditMode);

    if (this.readonlyMode) comp.setInput("readonlyMode", this.readonlyMode);

    comp.instance.startDraggingContainer.subscribe((data) => this.startDraggingContainer.emit(data));
    comp.instance.stopDraggingContainer.subscribe(() => this.stopDraggingContainer.emit());

    //this._children.push(child);
    this._childrenRef.push(comp);
  }

  fixOrder() {
    const refs = [...this._childrenRef];
    this._childrenRef = [];
    for (let i = 0; i < this.self.children.length; i++) {
      const ref = refs.find((r) => r.instance.self.id == this.self.children[i].id);
      if (ref) this._childrenRef.push(ref);
    }
    if (this._childrenRef.length != this.self.children.length) console.error("Couldn't fix Order!");
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!this._childrenRef) return;
    console.log("changes:", changes);

    for (const name in changes) {
      if (name == "self") this.loadChildren(); // refresh children
      else this._childrenRef.forEach((c) => c.setInput(name, changes[name].currentValue));
    }
  }

  remove(id: number) {
    for (let i = 0; i < this.self.children.length; i++) {
      const c = this.self.children[i];
      if (c.id == id) {
        // remove
        this.self.children.splice(i, 1);
        this._childrenRef[i].destroy();
        this._childrenRef.splice(i, 1);
        return true;
      }
      if (isContainer(c) && (this._childrenRef[i].instance as ContainerComponent<T>).remove(id)) return true;
    }
    return false;
  }

  insert(parentId: number, comp: Component) {
    if (this.self.id == parentId) {
      // insert
      this.self.children.push(comp);
      this.spawnChild(comp);
      return true;
    }
    for (let i = 0; i < this.self.children.length; i++) {
      const c = this.self.children[i];
      if (isContainer(c) && (this._childrenRef[i].instance as ContainerComponent<T>).insert(parentId, comp))
        return true;
    }
    return false;
  }

  updateData(id: number, data: Component) {
    for (let i = 0; i < this.self.children.length; i++) {
      const c = this.self.children[i];
      if (c.id == id) {
        this._childrenRef[i].instance.setData(data);
        return true;
      }
      if (isContainer(c) && (this._childrenRef[i].instance as ContainerComponent<T>).updateData(id, data)) return true;
    }
    return false;
  }

  findChild(id: number): Component {
    for (let i = 0; i < this.self.children.length; i++) {
      const c = this.self.children[i];
      if (c.id == id) return c;
      else if (isContainer(c)) {
        const found = (this._childrenRef[i].instance as ContainerComponent<T>).findChild(id);
        if (found) return found;
      }
    }
    return null;
  }
}
