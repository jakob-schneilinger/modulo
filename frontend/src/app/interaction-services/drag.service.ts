import { ElementRef, Injectable } from "@angular/core";
import { Container, Component as Comp, Board } from "../dtos/component";
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

  constructor(private resizeService: ResizeService) {}

  startDragging(data: { component: Comp; event: MouseEvent; preview: ElementRef }, sourceParent: Container) {
    data.event.preventDefault();
    this.draggingContainer = data.component;
    this.sourceParent = sourceParent;
    this.componentDepth = this.getComponentDepth(this.draggingContainer);

    this.preview = data.preview;
    console.log("start", this.preview, this.draggingContainer);
  }

  onMouseMove(event: MouseEvent, rootContainer: Container) {
    if (!this.draggingContainer) return;
    const rootElement = document.querySelector(`[data-id="${rootContainer.id}"]`);

    let targetElement = event.target as HTMLElement;
    while (
      !targetElement.hasAttribute("data-id") ||
      Number(targetElement.getAttribute("data-id")) === this.draggingContainer.id
    ) {
      targetElement = targetElement.parentElement;
      if (targetElement == document.body) return;
    }

    const target = this.findContainerById(Number(targetElement.getAttribute("data-id")), rootContainer);

    const grid = document.getElementById(`container-grid-${target?.id}`);
    if (!grid) return;

    const rect = grid.getBoundingClientRect();
    const deltaX = event.clientX - rect.left;
    const deltaY = event.clientY - rect.top;

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

    const hasCycle = this.hasCycle(target, this.draggingContainer);
    const totalDepth = this.getDepth(rootContainer, target.id);
    let hasCollision = this.resizeService.hasCollisions(target, testComponent);
    if (hasCollision == undefined) hasCollision = false;

    if (hasCycle) testComponent.name = "Recursive Components not allowed!";
    else if (totalDepth == -1) testComponent.name = "Component depth to deep!";
    else if (hasCollision) testComponent.name = "No Collisions allowed!";
    else testComponent.name = "";

    const invalidPosition = hasCollision || hasCycle || totalDepth == -1;

    const w = this.draggingContainer.width * columnWidth;
    const h = this.draggingContainer.height * rowHeight;
    const rootRect = rootElement.getBoundingClientRect();

    let x = event.clientX - rootRect.left + window.scrollX - w;
    x += columnWidth - (x % columnWidth);
    let y = event.clientY - rootRect.top;
    y -= y % rowHeight;

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

    p.style.backgroundColor = invalidPosition ? "rgba(255, 0, 0, 0.15)" : "rgba(0, 123, 255, 0.15)";
    p.style.border = invalidPosition ? "2px dashed rgba(255, 0, 0, 0.5)" : "2px dashed rgba(0, 123, 255, 0.5)";
    p.textContent = testComponent.name;

    this.previewColumn = column - this.draggingContainer.width + 1;
    this.previewRow = row;
    this.previewParent = target;
  }

  stopDragging(
    event: MouseEvent,
    rootContainer: Container,
    callback: (component: Comp, targetContainer: Container) => void
  ) {
    if (this.draggingContainer /* && this.previewContainer */ && this.sourceParent) {
      let testComponent: Board = {
        ...this.draggingContainer,
        column: this.previewColumn,
        row: this.previewRow,
        name: "",
        children: [],
      };

      const hasCycle = this.hasCycle(this.previewParent, this.draggingContainer);
      const totalDepth = this.getDepth(rootContainer, this.previewParent.id);
      let hasCollision = this.resizeService.hasCollisions(this.previewParent, testComponent);
      if (hasCollision == undefined) hasCollision = false;

      const invalidPosition = hasCollision || hasCycle || totalDepth == -1;

      console.log("hasCollision", hasCollision);
      console.log("hasCycle", hasCycle);
      console.log("totalDepth", totalDepth);

      let p = this.preview.nativeElement as HTMLElement;
      p.style.display = "none";

      if (invalidPosition) {
        console.log("TEST");
        callback(this.draggingContainer, this.sourceParent);
      } else {
        this.removeComponentFromAll(this.draggingContainer.id, rootContainer);
        this.draggingContainer.column = this.previewColumn;
        this.draggingContainer.row = this.previewRow;
        this.previewParent.children.push(this.draggingContainer);
        console.log("Column", this.previewColumn);
        console.log("Row", this.previewRow);
        callback(this.draggingContainer, this.previewParent);
      }

      //const targetContainer = this.findContainerById(parentId, rootContainer) || rootContainer;

      // Remove preview
      //this.removePreviewFromAll(rootContainer);

      /* const testComponent = {
        ...this.draggingContainer,
        column: this.previewContainer.column,
        row: this.previewContainer.row,
      };

      const hasCycle = this.hasCycle(targetContainer, this.draggingContainer);
      const totalDepth = this.getDepth(rootContainer, targetContainer.id);
      let hasCollision = this.resizeService.hasCollisions(targetContainer, testComponent);
      if (hasCollision == undefined) {
        hasCollision = false;
      } */
      /* if (hasCycle || hasCollision || totalDepth == -1) {
      } else {
        if (this.hasCycle(targetContainer, this.draggingContainer)) {
          callback(this.draggingContainer, this.sourceParent);
        } else {
          this.removeComponentFromAll(this.draggingContainer.id, rootContainer);
          this.draggingContainer.column = this.previewContainer.column;
          this.draggingContainer.row = this.previewContainer.row;
          targetContainer.children.push(this.draggingContainer);
          callback(this.draggingContainer, targetContainer);
        }
      } */
    }

    this.draggingContainer = null;
    this.sourceParent = null;
    this.preview = null;
  }

  /* insertPreview(preview: Container, rootContainer: Container) {
    const target = this.findContainerById(preview.parentId!, rootContainer);
    if (!target || !Array.isArray(target.children)) return;

    // Add preview
    target.children.push(preview);
  } */

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
    if (depth + this.componentDepth > 4 || !this.isContainer(baseContainer)) {
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

  /* private removePreviewFromAll(container: Container) {
    container.children = container.children.filter((c) => c.id !== -1);
    for (const child of container.children) {
      if (this.isContainer(child)) {
        this.removePreviewFromAll(child);
      }
    }
  } */

  private removeComponentFromAll(id: number, container: Container): void {
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
