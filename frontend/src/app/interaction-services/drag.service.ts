import { ElementRef, Injectable } from "@angular/core";
import {Container, Component as Comp, Board, Calendar} from "../dtos/component";
import { ResizeService } from "./resize.service";
import { gridVar } from "../dtos/grid";

@Injectable({
  providedIn: "root",
})
export class DragService {
  private draggingContainer: Comp | null = null;
  private preview: ElementRef;

  //private previewContainer: Container | null = null;
  private sourceParent: Container | null = null;
  private componentDepth: number;

  private previewRow: number;
  private previewColumn: number;
  private previewParent: Container;

  private calendar: Comp;

  constructor(private resizeService: ResizeService) {}

  startDragging(data: { component: Comp; event: MouseEvent | TouchEvent; preview: ElementRef }, sourceParent: Container) {
    data.event.preventDefault();
    this.draggingContainer = data.component;
    this.sourceParent = sourceParent;
    this.componentDepth = this.getComponentDepth(this.draggingContainer);

    this.preview = data.preview;
    console.log("start", this.preview, this.draggingContainer);
  }

  onMouseMove(event: MouseEvent | TouchEvent, rootContainer: Container) {

    if (!this.draggingContainer) return;
    let e;
    if (typeof TouchEvent !== 'undefined' && event instanceof TouchEvent && 'touches' in event) {
      if (event.touches.length === 1) {
        e = event.touches[0];
      } else {
        return;
      }
    } else {
      e = event;
    }

    const rootElement = document.querySelector(`[data-id="${rootContainer.id}"]`);
    let targetElement = e.target as HTMLElement;

    if (!document.body.contains(targetElement)) {
      return;
    }

    this.calendar = undefined;

    while (
      !targetElement.hasAttribute("data-id") ||
      Number(targetElement.getAttribute("data-id")) === this.draggingContainer.id
      ) {
      targetElement = targetElement.parentElement;

      if (targetElement.hasAttribute("data-calendar-id") && this.draggingContainer?.type === 'task') {
        const targetCalendar = this.findCalendarById(Number(targetElement.getAttribute("data-calendar-id")), rootContainer);
        if(targetCalendar != null && targetCalendar.type == 'calendar') {
          this.calendar = targetCalendar;
        }
      }

      if (targetElement == document.body || targetElement == null) return;
    }


    const target = this.findContainerById(Number(targetElement.getAttribute("data-id")), rootContainer);

/*
    if (this.draggingContainer?.type === 'task') {
      const calendarTarget = this.findCalendarById(Number(targetElement.getAttribute("data-id")), rootContainer);
      console.log(calendarTarget);
      if(calendarTarget != null && calendarTarget.type == 'calendar') {
        console.log(this.draggingContainer?.type + calendarTarget?.type);
      }
    }

 */

    const grid = document.getElementById(`container-grid-${target?.id}`);
    if (!grid) return;

    const rect = grid.getBoundingClientRect();
    const deltaX = e.clientX - rect.left;
    const deltaY = e.clientY - rect.top;

    const columnWidth = grid.offsetWidth / gridVar.columns;
    const rowHeight = gridVar.rowHeight;

    const column = Math.max(
      this.draggingContainer.width,
      Math.min(gridVar.columns, Math.floor(deltaX / columnWidth)) + 1
    );
    const row = Math.max(1, Math.floor(deltaY / rowHeight) + 1);

    let testComponent: Board = {
      ...this.draggingContainer,
      column: column - this.draggingContainer.width + 1,
      row,
      name: "",
      children: [],
    };

    const w = this.draggingContainer.width * columnWidth;
    const h = this.draggingContainer.height * rowHeight;
    const rootRect = rootElement.getBoundingClientRect();

    let x = e.clientX - rootRect.left + window.scrollX - w;
    x += columnWidth - (x % columnWidth);
    let y = e.clientY - rootRect.top;
    y -= y % rowHeight;

    let totalDepth= this.getDepth(rootContainer, target.id);
    let invalidPosition = false;

    if (!this.calendar) {

      const hasCycle = this.hasCycle(target, this.draggingContainer);
      let hasCollision = this.resizeService.hasCollisions(target, testComponent);
      if (hasCollision == undefined) hasCollision = false;

      if (hasCycle) testComponent.name = "Recursive Components not allowed!";
      else if (totalDepth == -1) testComponent.name = "Component depth to deep!";
      else if (hasCollision) testComponent.name = "No Collisions allowed!";
      else testComponent.name = "";

      invalidPosition = hasCollision || hasCycle || totalDepth == -1;

    }

    x += 8 * totalDepth;
    y += 8 * totalDepth;

    let p = this.preview.nativeElement as HTMLElement;
    p.style.position = "absolute";
    p.style.left = `${x}px`;
    p.style.top = `${y}px`;
    p.style.width = `${w}px`;
    p.style.height = `${h}px`;
    p.style.display = "block";
    p.style.zIndex = "5";
    p.style.pointerEvents = "none";

    if (this.calendar) {
      p.style.backgroundColor = "rgba(83, 165, 113, 0.15)";
      p.style.border = "2px dashed rgba(83, 165, 113, 0.5)";
    } else {
      p.style.backgroundColor = invalidPosition ? "rgba(255, 0, 0, 0.15)" : "rgba(0, 123, 255, 0.15)";
      p.style.border = invalidPosition ? "2px dashed rgba(255, 0, 0, 0.5)" : "2px dashed rgba(0, 123, 255, 0.5)";
    }
    p.textContent = testComponent.name;

    this.previewColumn = column - this.draggingContainer.width + 1;
    this.previewRow = row;
    this.previewParent = target;
  }

  stopDragging(
    event: MouseEvent | TouchEvent,
    rootContainer: Container,
    callback: (component: Comp, targetContainer: Comp) => void
  ) {
    if (this.draggingContainer /* && this.previewContainer */ && this.sourceParent) {
      let testComponent: Board = {
        ...this.draggingContainer,
        column: this.previewColumn,
        row: this.previewRow,
        name: "",
        children: [],
      };

      let invalidPosition = false;

      if (!this.calendar) {
        const hasCycle = this.hasCycle(this.previewParent, this.draggingContainer);
        const totalDepth = this.getDepth(rootContainer, this.previewParent.id);
        let hasCollision = this.resizeService.hasCollisions(this.previewParent, testComponent);
        if (hasCollision == undefined) hasCollision = false;

        invalidPosition = hasCollision || hasCycle || totalDepth == -1;
      }

      let p = this.preview.nativeElement as HTMLElement;
      p.style.display = "none";

      if (invalidPosition) {
        callback(this.draggingContainer, this.sourceParent);
      } else {

        if (this.calendar) {
          callback(this.draggingContainer, this.calendar);
        } else {
          //this.removeComponentFromAll(this.draggingContainer.id, rootContainer);
          this.draggingContainer.column = this.previewColumn;
          this.draggingContainer.row = this.previewRow;
          //this.previewParent.children.push(this.draggingContainer);
          callback(this.draggingContainer, this.previewParent);
        }
      }
    }

    this.draggingContainer = null;
    this.sourceParent = null;
    this.preview = null;
  }

  hasCycle(target: Comp, component: Comp) {
    if (!(component as any).children) return;
    const children = (component as Container).children;
    for (let i = 0; i < children?.length; i++) {
      const child = children[i];
      if (child.id == target.id) {
        return true;
      }
      if ((child as any).children?.length > 0) {
        return this.hasCycle(target, child);
      }
    }
    return false;
  }

  private getDepth(baseContainer: Comp, targetId: number, depth: number = 0) {
    if (depth + this.componentDepth > (this.sourceParent as Board).depth || !this.isContainer(baseContainer)) {
      return -1;
    }
    if (baseContainer.id === targetId) {
      return depth + this.componentDepth;
    }
    if (baseContainer) {
      for (const child of baseContainer.children) {
        const result = this.getDepth(child, targetId, depth + 1);
        if (result !== -1) return result;
      }
    }
    return -1;
  }

  private getComponentDepth(component: Comp): number {
    if (!(component as Container).children) return 1;
    let depth = 1;
    for (let i = 0; i < (component as Container).children?.length; i++) {
      const childDepth = this.getComponentDepth((component as Container).children[i]) + 1;
      if (depth < childDepth) {
        depth = childDepth;
      }
    }
    return depth;
  }

  private findContainerById(id: number, container: Container): Container | null {
    if (!container) return null;
    if (container.id === id) return container;
    for (const child of container.children) {
      if (this.isContainer(child)) {
        const found = this.findContainerById(id, child);
        if (found) return found;
      }
    }
    return null;
  }

  private findCalendarById(id:number, container: Container): Comp | null{
    if (!container) return null;
    for (const child of container.children) {
      if(id == child.id && child.type == 'calendar') return child;
      if (this.isContainer(child)) {
        const found = this.findCalendarById(id, child);
        if (found) return found;
      }
    }
    return null;
  }

  private removeComponentFromAll(id: number, container: Container): void {
    alert("Don't call here! Deprecated. Will be handled in Home");

    container.children = container.children.filter((c) => c.id !== id);
    for (const child of container.children) {
      if (this.isContainer(child)) {
        this.removeComponentFromAll(id, child);
      }
    }
  }

  private isContainer(component: any): component is Container {
    return (
      (component.type === "board" || component.type === "task" || component.type === "note") &&
      Array.isArray(component.children)
    );
  }
}
