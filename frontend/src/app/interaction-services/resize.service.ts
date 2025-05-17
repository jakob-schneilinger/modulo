import { Injectable, ElementRef } from '@angular/core';
import { Container, Component as Comp } from '../dtos/component';

@Injectable({
  providedIn: 'root'
})
export class ResizeService {
  private isResizing = false;
  private activeResizerId: string | null = null;

  startResize(event: MouseEvent, component: Comp, cardEl: ElementRef, previewCardEl: ElementRef) {
    const target = event.target as HTMLElement;
    if (target.classList.contains('resizer') && target.id === `resizer-${component.id}`) {
      this.isResizing = true;
      this.activeResizerId = target.id;
    }
  }

  resize(event: MouseEvent, component: Comp, currentWidth: number, cardEl: ElementRef, previewCardEl: ElementRef, parentGridEl: ElementRef | null, homeGridEl: ElementRef | null, depth: number, callback: (columns: number, rows: number) => void) {
    if (!this.isResizing || this.activeResizerId !== `resizer-${component.id}`) return;

    const headerEls = document.querySelectorAll('.container-header h5');
    headerEls.forEach(el => (el as HTMLElement).style.userSelect = 'none');

    const card = cardEl.nativeElement as HTMLElement;
    const preview = previewCardEl.nativeElement as HTMLElement;
    const rect = card.getBoundingClientRect();
    const deltaX = event.clientX - rect.left;
    const deltaY = event.clientY - rect.top;

    const grid = depth > 1 ? (parentGridEl?.nativeElement ?? card) : (homeGridEl?.nativeElement ?? card);
    const columnWidth = grid.offsetWidth / 8;

    const rows = Math.min(8, Math.max(1, Math.round(deltaY / 150))); // TODO: change if Css changes

    if(columnWidth != 0) {
      const columns = Math.min(8, Math.max(1, Math.round(deltaX / columnWidth)));

      preview.style.position = 'absolute';
      preview.style.top = `${card.offsetTop}px`;
      preview.style.left = `${card.offsetLeft}px`;
      preview.style.width = `${deltaX}px`;
      preview.style.height = `${deltaY}px`;
      preview.style.display = 'block';
      preview.style.zIndex = '10';
      preview.style.pointerEvents = 'none';

      callback(columns, rows);
    } else {
      callback(currentWidth, rows);
    }
  }

  stopResize(event: MouseEvent, component: Comp, currentWidth:number, currentHeight:number, previewCardEl: ElementRef, parentContainer: Container, callback: (columns: number, rows: number) => void) {
    if (!this.isResizing || this.activeResizerId !== `resizer-${component.id}`) return;

    this.isResizing = false;
    this.activeResizerId = null;

    const headerEls = document.querySelectorAll('.container-header h5');
    headerEls.forEach(el => (el as HTMLElement).style.userSelect = '');

    const preview = previewCardEl.nativeElement as HTMLElement;

    preview.style.display = 'none';

    if (currentWidth !== component.width || currentHeight !== component.height) {
      component.width = currentWidth;
      component.height = currentHeight;
      this.handleCollisions(parentContainer);
    }

    callback(currentWidth, currentHeight);
  }

  // TODO: update in backend if collision appear
  handleCollisions(parentContainer: Container) {
    const children = parentContainer.children;

    children.sort((a, b) => {
      if (a.row === b.row) return a.column - b.column;
      return a.row - b.row;
    });

    for (let i = 0; i < children.length; i++) {
      const current = children[i];

      for (let j = i + 1; j < children.length; j++) {
        const compare = children[j];

        if (this.isOverlapping(current, compare)) {
          compare.row = current.row + current.height;
          this.handleCollisions(parentContainer);
          return;
        }
      }
    }
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

    /*
    TODO: remove if done with debugging
    console.log("Left: " +aLeft+ " / " +bLeft);
    console.log("Right: " +aRight+ " / " +bRight);
    console.log("Top: " +aTop+ " / " +bTop);
    console.log("Bottom: " +aBottom+ " / " +bBottom);

     */

    return !(aRight <= bLeft || aLeft >= bRight || aBottom <= bTop || aTop >= bBottom);
  }
}
