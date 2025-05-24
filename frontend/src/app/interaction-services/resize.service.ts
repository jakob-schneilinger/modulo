import { Injectable, ElementRef } from '@angular/core';
import { Container, Component as Comp } from '../dtos/component';
import { gridVar } from '../dtos/grid';

@Injectable({
  providedIn: 'root'
})
export class ResizeService {
  private isResizing = false;
  private activeResizerId: string | null = null;
  private startWidth: number = 1;
  private startHeight: number = 1;


  startResize(event: MouseEvent, component: Comp, cardEl: ElementRef, previewCardEl: ElementRef) {
    const target = event.target as HTMLElement;
    if (target.classList.contains('resizer') && target.id === `resizer-${component.id}`) {
      this.isResizing = true;
      this.activeResizerId = target.id;
      this.startWidth = component.width;
      this.startHeight = component.height;
    }
  }

  resize(event: MouseEvent, component: Comp, currentWidth: number, cardEl: ElementRef, previewCardEl: ElementRef, parentContainer: Container, parentGridEl: ElementRef | null, homeGridEl: ElementRef | null, depth: number, callback: (columns: number, rows: number) => void) {
    if (!this.isResizing || this.activeResizerId !== `resizer-${component.id}`) return;

    const body = document.querySelectorAll('body');
    body.forEach(b => b.style.userSelect = 'none')

    const card = cardEl.nativeElement as HTMLElement;
    const preview = previewCardEl.nativeElement as HTMLElement;
    const rect = card.getBoundingClientRect();
    const deltaX = event.clientX - rect.left;
    const deltaY = event.clientY - rect.top;

    const grid = depth > 1 ? (parentGridEl?.nativeElement ?? card) : (homeGridEl?.nativeElement ?? card);
    const columnWidth = grid.offsetWidth / gridVar.columns;
    const rowHeight = gridVar.rowHeight; //grid.offsetHeight / 8;

    const rows = Math.min(gridVar.columns, Math.max(1, Math.round(deltaY / rowHeight))); // TODO: change if Css changes

    if(columnWidth != 0) {
      const columns = Math.min(gridVar.columns, Math.max(1, Math.round(deltaX / columnWidth)));

      preview.style.position = 'absolute';
      preview.style.top = `${card.offsetTop}px`;
      preview.style.left = `${card.offsetLeft}px`;
      preview.style.width = `${deltaX}px`;
      preview.style.height = `${deltaY}px`;
      preview.style.display = 'block';
      preview.style.zIndex = '10';
      preview.style.pointerEvents = 'none';

      const testComponent = { ...component, width: columns, height: rows };
      const hasCollision = this.hasCollisions(parentContainer, testComponent);
      preview.style.backgroundColor = hasCollision ? 'rgba(255, 0, 0, 0.15)' : 'rgba(0, 123, 255, 0.15)';
      preview.style.border = hasCollision ? '2px dashed rgba(255, 0, 0, 0.5)' : '2px dashed rgba(0, 123, 255, 0.5)';

      callback(columns, rows);
    } else {
      callback(currentWidth, rows);
    }
  }

  stopResize(event: MouseEvent, component: Comp, currentWidth: number, currentHeight: number, previewCardEl: ElementRef, parentContainer: Container, callback: (columns: number, rows: number, changes: boolean) => void) {
    if (!this.isResizing || this.activeResizerId !== `resizer-${component.id}`) return;

    this.isResizing = false;
    this.activeResizerId = null;

    const body = document.querySelectorAll('body');
    body.forEach(b => b.style.userSelect = '')

    const preview = previewCardEl.nativeElement as HTMLElement;

    preview.style.display = 'none';

    if (currentWidth !== component.width || currentHeight !== component.height) {

      const testComponent = { ...component, width: currentWidth, height: currentHeight };

      if (this.hasCollisions(parentContainer, testComponent)) {
        callback(this.startWidth, this.startHeight, false);
        return;
      }

      component.width = currentWidth;
      component.height = currentHeight;
      callback(currentWidth, currentHeight, true);
    }

    callback(currentWidth, currentHeight, false);
  }

  hasCollisions(parentContainer: Container, component: Comp): boolean {
    const children: Comp[] = parentContainer.children;

    for (let i = 0; i < children.length; i++) {
      if (this.isOverlapping(children[i], component) && component.id !== children[i].id) {
        if (children[i].id === -1) {
          return undefined;
        }
        return true;
      }
    }
    return false;
  }

  private isOverlapping(a: Comp, b: Comp): boolean {
    const aLeft = a.column;
    const aRight = a.column + a.width;
    const aTop = a.row;
    const aBottom = a.row + a.height;

    const bLeft = b.column;
    const bRight = b.column + b.width;
    const bTop = b.row;
    const bBottom = b.row + b.height;

    return !(aRight <= bLeft || aLeft >= bRight || aBottom <= bTop || aTop >= bBottom);
  }
}
