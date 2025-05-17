import { Injectable } from '@angular/core';
import { Container, Component as Comp } from '../dtos/component';
import { ResizeService } from './resize.service';

@Injectable({
  providedIn: 'root'
})
export class DragService {
  private dragging = false;
  private draggingContainer: Comp | null = null;
  private previewContainer: Container | null = null;
  private sourceParent: Container | null = null;

  constructor(private resizeService: ResizeService) {}

  startDragging(data: { component: Comp, event: MouseEvent }, sourceParent: Container) {
    data.event.preventDefault();
    this.draggingContainer = data.component;
    this.sourceParent = sourceParent;
    this.dragging = true;

    this.previewContainer = {
      ...data.component,
      type: 'board',
      id: -1,
      name: '',
      children: [],
    };

  }

  onMouseMove(event: MouseEvent, rootContainer: Container, callback: (container: Container, targetContainer: Container) => void) {
    if (!this.draggingContainer || !this.previewContainer) return;

    const elementUnderMouse = document.elementFromPoint(event.clientX, event.clientY);
    let targetContainer: Container = rootContainer;
    let containerId = '0';

    let containerEl = elementUnderMouse instanceof HTMLElement ? elementUnderMouse : null;
    while (containerEl && (!containerEl.classList.contains('card') || containerEl.getAttribute('data-id') === '-1')) {
      containerEl = containerEl.parentElement;
    }

    if (containerEl && containerEl instanceof HTMLElement) {
      containerId = containerEl.getAttribute('data-id') || '0';
      const target = this.findContainerById(Number(containerId), rootContainer);
      if (target && target.id !== this.draggingContainer.id && target.children) {
        targetContainer = target;
      }
    }

    const grid = document.getElementById(`container-grid-${targetContainer.id}`);
    if (!grid) return;

    const rect = grid.getBoundingClientRect();
    const columnWidth = rect.width / 8;
    const rowHeight = 150; // TODO: Change if Css changes

    const relativeX = event.clientX - rect.left;
    const relativeY = event.clientY - rect.top;

    const newColumn = Math.max(1, Math.min(8, Math.round(relativeX / columnWidth)));
    const newRow = Math.max(1, Math.min(8, Math.floor(relativeY / rowHeight) + 1));

    this.previewContainer.column = newColumn;
    this.previewContainer.row = newRow;

    console.log(containerId)

    this.previewContainer.parent_id = targetContainer.id;
    this.removePreviewFromAll(rootContainer);
    this.insertPreview(this.previewContainer, rootContainer);

    callback(this.previewContainer, targetContainer);
  }

  stopDragging(event: MouseEvent, rootContainer: Container, callback: (component: Comp, targetContainer: Container) => void) {
    if (this.draggingContainer && this.previewContainer && this.sourceParent) {
      const targetContainer = this.findContainerById(this.previewContainer.parent_id!, rootContainer) || rootContainer;

      // Remove preview
      this.removePreviewFromAll(rootContainer);

      // Remove from source parent
      this.removeComponentFromAll(this.draggingContainer.id, rootContainer);

      // Update dragged container's position
      this.draggingContainer.column = this.previewContainer.column;
      this.draggingContainer.row = this.previewContainer.row;

      // Add to target container
      targetContainer.children.push(this.draggingContainer);

      // Handle collisions in target container
      this.resizeService.handleCollisions(targetContainer);

      callback(this.draggingContainer, targetContainer);
    }

    this.draggingContainer = null;
    this.previewContainer = null;
    this.sourceParent = null;
    this.dragging = false;
  }

  /*
  insertPreview(preview: Container, parentContainer: Container) {
    const children = parentContainer.children;
    const index = children.findIndex(c => c.id === -1);
    if (index !== -1) {
      children.splice(index, 1);
    }
    children.push(preview);
  }

   */

  insertPreview(preview: Container, rootContainer: Container) {
    const target = this.findContainerById(preview.parent_id!, rootContainer);
    if (!target || !Array.isArray(target.children)) return;

    // Add preview
    target.children.push(preview);
  }

  private findContainerById(id: number, container: Container): Container | null {
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
    console.log("Test Preview")
    container.children = container.children.filter(c => c.id !== -1);
    for (const child of container.children) {
      if (this.isContainer(child)) {
        this.removePreviewFromAll(child);
      }
    }
  }

  private removeComponentFromAll(id: number, container: Container): void {
    console.log("Test Component")
    container.children = container.children.filter(c => c.id !== id);
    for (const child of container.children) {
      if (this.isContainer(child)) {
        this.removeComponentFromAll(id, child);
      }
    }
  }


  private isContainer(component: any): component is Container {
    return (
      (component.type === 'board' ||
        component.type === 'task' ||
        component.type === 'note') &&
      Array.isArray(component.children)
    );
  }
}
