import { Injectable } from "@angular/core";
import { Container, Component as Comp } from "../dtos/component";
import { ResizeService } from "./resize.service";
import {gridVar} from "../dtos/grid";

@Injectable({
  providedIn: "root",
})
export class DragService {
  private dragging = false;
  private draggingContainer: Comp | null = null;
  private previewContainer: Container | null = null;
  private sourceParent: Container | null = null;
  private componentDepth: number;

  constructor(private resizeService: ResizeService) {}

  startDragging(data: { component: Comp; event: MouseEvent }, sourceParent: Container) {
    data.event.preventDefault();
    this.draggingContainer = data.component;
    this.sourceParent = sourceParent;
    this.dragging = true;
    this.componentDepth = this.getComponentDepth(this.draggingContainer);

    this.previewContainer = {
      ...data.component,
      type: "board",
      id: -1,
      name: "",
      children: [],
    };
  }

  onMouseMove(
    event: MouseEvent,
    rootContainer: Container,
    callback: (container: Container, targetContainer: Container) => void
  ) {
    if (!this.draggingContainer || !this.previewContainer) return;

    const elementUnderMouse = document.elementFromPoint(event.clientX, event.clientY);
    let targetContainer: Container = rootContainer;

    let containerEl = elementUnderMouse instanceof HTMLElement ? elementUnderMouse : null;

    let foundValidTarget = false;
    while (containerEl && !foundValidTarget) {
      const containerIdAttr = containerEl.getAttribute("data-id");
      if (containerIdAttr && containerIdAttr !== "-1") {
        const containerId = Number(containerIdAttr);
        if (containerId !== this.draggingContainer.id) {
          const target = this.findContainerById(containerId, rootContainer);
          if (target && target.children) {
            targetContainer = target;
            foundValidTarget = true;
          }
        }
      }
      if (!foundValidTarget) {
        containerEl = containerEl.parentElement;
      }
    }

    const grid = document.getElementById(`container-grid-${targetContainer?.id}`);
    if (!grid) return;

    const rect = grid.getBoundingClientRect();
    const columnWidth = rect.width / gridVar.columns;
    const rowHeight = gridVar.rowHeight; // TODO: Change if Css changes

    const relativeX = event.clientX - rect.left;
    const relativeY = event.clientY - rect.top;

    const newColumn = Math.max(1, Math.min(gridVar.columns, Math.floor(relativeX / columnWidth)));
    const newRow = Math.max(1, Math.min(gridVar.columns, Math.floor(relativeY / rowHeight) + 1));

    this.previewContainer.column = newColumn;
    this.previewContainer.row = newRow;

    this.previewContainer.parentId = targetContainer.id;

    let testComponent = {
      ...this.draggingContainer,
      column: this.previewContainer.column,
      row: this.previewContainer.row,
    };

    const hasCycle = this.hasCycle(targetContainer, this.draggingContainer);
    const totalDepth = this.getDepth(rootContainer, targetContainer.id);
    let hasCollision = this.resizeService.hasCollisions(targetContainer, testComponent);
    if (hasCollision == undefined) {
      hasCollision = false;
    }

    if (hasCycle) {
      this.previewContainer.name = "Recursive Components not allowed!"
    } else if (totalDepth == -1) {
      this.previewContainer.name = "Component depth to deep!"
    } else if (hasCollision) {
      this.previewContainer.name = "No Collissions allowed!"
    } else {
      this.previewContainer.name = ""
    }

    const invalidPosition = hasCollision || hasCycle || totalDepth == -1;

    const previewEl = document.querySelector(`.card[data-id='-1']`) as HTMLElement | null;
    if (previewEl) {
      previewEl.style.backgroundColor = invalidPosition ? "rgba(255, 0, 0, 0.15)" : "rgba(0, 123, 255, 0.15)";
      previewEl.style.border = invalidPosition ? "2px dashed rgba(255, 0, 0, 0.5)" : "2px dashed rgba(0, 123, 255, 0.5)";
    }

    this.removePreviewFromAll(rootContainer);
    this.insertPreview(this.previewContainer, rootContainer);

    callback(this.previewContainer, targetContainer);
  }

  stopDragging(
    event: MouseEvent,
    rootContainer: Container,
    callback: (component: Comp, targetContainer: Container) => void
  ) {
    if (this.draggingContainer && this.previewContainer && this.sourceParent) {
      const targetContainer = this.findContainerById(this.previewContainer.parentId!, rootContainer) || rootContainer;

      // Remove preview
      this.removePreviewFromAll(rootContainer);

      const testComponent = {
        ...this.draggingContainer,
        column: this.previewContainer.column,
        row: this.previewContainer.row,
      };

      const hasCycle = this.hasCycle(targetContainer, this.draggingContainer);
      const totalDepth = this.getDepth(rootContainer, targetContainer.id);
      let hasCollision = this.resizeService.hasCollisions(targetContainer, testComponent);
      if (hasCollision == undefined) {
        hasCollision = false;
      }

      if (hasCycle || hasCollision || totalDepth == -1) {
        callback(this.draggingContainer, this.sourceParent);
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
      }
    }

    this.draggingContainer = null;
    this.previewContainer = null;
    this.sourceParent = null;
    this.dragging = false;
  }

  insertPreview(preview: Container, rootContainer: Container) {
    const target = this.findContainerById(preview.parentId!, rootContainer);
    if (!target || !Array.isArray(target.children)) return;

    // Add preview
    target.children.push(preview);
  }

  hasCycle(target: Comp, component: Comp) {
    const children = component.children;
    for(let i = 0; i < children?.length; i++) {
      const child = children[i]
      if (child.id == target.id) {
        return true;
      }
      if (child.children?.length > 0) {
        return this.hasCycle(target, child)
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

  private getComponentDepth(component: Comp):number {
    let depth = 1;
    for (let i = 0; i < component.children?.length; i++) {
      const childDepth = this.getComponentDepth(component.children[i]) + 1
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

  private removePreviewFromAll(container: Container) {
    container.children = container.children.filter((c) => c.id !== -1);
    for (const child of container.children) {
      if (this.isContainer(child)) {
        this.removePreviewFromAll(child);
      }
    }
  }

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
