import { Board, Container, Text, Component as Comp, Note, Task, isType } from "../../dtos/component";
import { ComponentService } from "../../services/component.service";
import { DragService } from "../../interaction-services/drag.service";
import { AuthService } from "../../services/auth.service";
import { ActivatedRoute, Router } from "@angular/router";
import { Component, OnInit, ViewChild, ElementRef, OnDestroy } from "@angular/core";
import { EventService } from "../../interaction-services/event.service";
import { Subscription } from "rxjs";
import { gridVar } from "../../dtos/grid";

// for delete modal
declare var bootstrap: any;

@Component({
  selector: "app-home",
  templateUrl: "./home.component.html",
  styleUrls: ["./home.component.scss", "../comp/base/base.component.scss"],
  standalone: false,
})
export class HomeComponent implements OnInit, OnDestroy {
  constructor(
    public authService: AuthService,
    public router: Router,
    public route: ActivatedRoute,
    public compService: ComponentService,
    private dragService: DragService,
    private eventService: EventService
  ) {}

  @ViewChild("name") nameElement;
  @ViewChild("deleteModal") deleteModal: ElementRef;
  @ViewChild("preview", { static: false }) previewEl!: ElementRef;

  inEditMode: boolean = false;

  private subscriptions: Subscription[] = [];

  // used for component dragging
  dragging = false;

  // essentially all boards we are going to show, so all the root boards that have no parent
  component: Board = undefined;
  forDeletion: Comp = undefined;

  createBoard() {
    const width = Math.floor(gridVar.columns / 4);
    const height = Math.floor(gridVar.columns / 4);
    const row = this.findFirstFreeRow(width, height);

    const newBoard: Board = {
      children: [],
      column: 1,
      height: height,
      name: "My New Board",
      type: "board",
      width: width,
      row: row,
      parentId: this.component.id,
    };

    this.compService.createBoard(newBoard).subscribe({
      next: (created) => this.addChild(created),
      error: (err) => console.error(err),
    });
  }

  createText() {
    const width = Math.floor(gridVar.columns / 4);
    const height = Math.floor(gridVar.columns / 8);
    const row = this.findFirstFreeRow(width, height);

    const newText: Text = {
      column: 1,
      height: Math.floor(gridVar.columns / 8),
      content: "Add Text here:",
      width: Math.floor(gridVar.columns / 4),
      type: "text",
      row: row,
      parentId: this.component.id,
    };

    this.compService.createText(newText).subscribe({
      next: (created) => this.addChild(created),
      error: (err) => console.error(err),
    });
  }

  createImage(isSketch: boolean = false) {
    const width = Math.floor(gridVar.columns / 4);
    const height = Math.floor(gridVar.columns / 4);
    const row = this.findFirstFreeRow(width, height);
    const column = 1;
    this.compService
      .createImage({ parentId: this.component.id, column, row, width: width, height: height }, null)
      .subscribe({
        next: (comp) => this.addChild({ ...comp, sketch: isSketch } as Comp),
        error: (e) => console.log(e),
      });
  }

  createNote() {
    const neighbors = this.component.children;
    const width = Math.floor(gridVar.columns / 2);
    const height = Math.floor(gridVar.columns / 4);

    const createDto: Partial<Note> = {
      type: "note",
      row: this.getNextUnusedRow(neighbors),
      column: 1,
      width,
      height,
      title: "New Note",
      labels: [{ name: "new" }],
      content: "# Hello World\n\n`This is`: *bold* and **italic** text!\n- num1\n- num2",
      parentId: this.component.id,
    };
    this.compService.createNote(createDto).subscribe({
      next: (comp) => this.addChild(comp),
      error: (e) => console.error(e),
    });
  }

  createTask() {
    const width = Math.floor(gridVar.columns / 4);
    const height = Math.floor(gridVar.columns / 3);
    const row = this.findFirstFreeRow(width, height);

    const newTask: Task = {
      children: [],
      column: 1,
      completed: false,
      height: height,
      name: "New Task",
      row: row,
      repeating: false,
      startDate: undefined,
      type: "task",
      width: width,
      parentId: this.component.id,
    };

    this.compService.createTask(newTask).subscribe({
      next: (created) => this.addChild(created),
      error: (err) => console.error(err),
    });
  }

  addChild(comp: Comp) {
    this.component.children.push(comp);
    this.component = { ...this.component };
  }

  updateName() {
    const update = { id: this.component.id, name: this.component.name };
    this.compService.updateBoard(update as any).subscribe({
      next: () => console.log("Board renamed!"),
      error: (e) => console.error(e),
    });
  }

  previewName() {
    window.dispatchEvent(new CustomEvent("board-name-changed", { detail: this.component }));
  }

  private getNextUnusedRow(neighbors: Comp[]): number {
    if (neighbors.length === 0) {
      return 1;
    }
    const sorted = neighbors.sort((a, b) => b.row - a.row);
    return sorted[0].row + sorted[0].height;
  }

  onWidthChanged(event: { component: Comp }) {
    this.compService.updatePosAndSize(event.component as any).subscribe({
      next: () => console.log("Updated successfully"),
      error: (err) => console.error("Update failed", err),
    });
  }

  titleChanged(event: { component: Comp }) {
    alert("Deprecated, don't call!");
    /* this.compService.updatePosAndSize(event.component as any).subscribe({
      next: () => console.log("Updated successfully"),
      error: (err) => console.error("Update failed", err),
    }); */
  }

  findAndPatch(nodes: Comp[], updated: Comp): boolean {
    for (const node of nodes) {
      if (node.id === updated.id) {
        Object.assign(node, updated);
        return true;
      }
      if (!(node as Container).children) continue;
      else if (this.findAndPatch((node as Container).children, updated)) {
        return true;
      }
    }
    return false;
  }

  taskChanged(event: { component: Comp }) {
    this.setRecursiveParentId(this.component);
    this.compService.updateTask(event.component as any).subscribe({
      next: (data) => this.findAndPatch(this.component.children, data),
      error: (err) => console.error("Update failed", err),
    });
  }

  taskRepeat(event: { component: Comp }) {
    this.setRecursiveParentId(this.component);
    this.compService.repeatTask(event.component as any).subscribe({
      next: (data) => {
        this.findAndPatch(this.component.children, data);
      },
      error: (err) => console.error("Update failed", err),
    });
  }

  textChanged(event: { component: Comp }) {
    this.compService.updateText(event.component as Text).subscribe({
      next: () => console.log("Updated successfully"),
      error: (err) => console.error("Update failed", err),
    });
  }

  createChild(type: string) {
    // TODO: add other components
    switch (type) {
      case "board":
        this.createBoard();
        break;
      case "text":
        this.createText();
        break;
      case "image":
        this.createImage();
        break;
      case "task":
        this.createTask();
        break;
      case "note":
        this.createNote();
        break;
      case "sketch":
        this.createImage(true);
        break;
      default:
        alert("not implemented yet!");
        console.error("unsupported type of component");
    }
  }

  deleteBoard() {
    this.compService.deleteComponent(this.component.id).subscribe({
      next: (value) => {
        window.dispatchEvent(
          new CustomEvent("board-delete", { detail: { id: this.component.id, name: this.component.name } })
        );
        this.router.navigate(["/"]);
      },
    });
  }

  private findFirstFreeRow(width: number, height: number, column: number = 1): number {
    const children = this.component.children;

    let row = 1;
    let found = false;

    while (!found) {
      // Define the new board's area
      const newArea = {
        left: column,
        right: column + width,
        top: row,
        bottom: row + height,
      };

      // Check for collision with any existing board
      const collision = children.some((child) => {
        const childArea = {
          left: child.column,
          right: child.column + child.width,
          top: child.row,
          bottom: child.row + child.height,
        };

        return !(
          newArea.right <= childArea.left ||
          newArea.left >= childArea.right ||
          newArea.bottom <= childArea.top ||
          newArea.top >= childArea.bottom
        );
      });

      if (!collision) {
        found = true;
      } else {
        row++; // move down and try again
      }
    }
    return row;
  }

  startDragging(data: { component: Comp; preview: ElementRef; event: MouseEvent }) {
    this.dragging = true;
    data.preview = this.previewEl;
    this.dragService.startDragging(data, <Container>this.component);
  }

  onMouseMove(event: MouseEvent) {
    this.dragService.onMouseMove(event, <Container>this.component);
  }

  stopDragging(event: MouseEvent) {
    if (!this.dragging) return;
    this.dragging = false;
    this.dragService.stopDragging(event, <Container>this.component, (container, targetContainer) => {
      container.parentId = targetContainer.id;
      this.compService.updatePosAndSize({ ...container, parentId: targetContainer.id } as any).subscribe({
        next: (value) => {
          this.component = { ...this.component };
        },
        error: (err) => {
          console.log("Error on dragging", err);
        },
      });
    });
  }

  getCompDeleteMsg(comp: Comp<any>): string {
    return `Are you sure you want to delete the component ${(comp as any)?.name || `of type ${comp.type}`}?`;
  }

  // Opens delete Modal with specific component
  openDeleteModal(component: Comp) {
    this.forDeletion = component;
    const modal = new bootstrap.Modal(this.deleteModal.nativeElement);
    modal.show();
  }

  // Deletes the component from the delete modal
  finishDeleteModal() {
    if (this.forDeletion.id == this.component.id) {
      this.deleteBoard();
    } else {
      this.compService.deleteComponent(this.forDeletion.id).subscribe({
        next: (value) =>
          (this.component = {
            ...this.component,
            children: this.recursiveDelete(this.component.children, this.forDeletion.id),
          }),
        error: (err) => console.error("unable to delete component", this.forDeletion.id, "with error", err),
      });
    }
  }

  logOut() {
    this.authService.logoutUser();
    this.router.navigate(["/login"]);
  }

  deleteUser() {}

  ngOnInit() {
    window.addEventListener("mousemove", this.onMouseMove.bind(this));
    window.addEventListener("mouseup", this.stopDragging.bind(this));

    // for a user fetch his root boards

    this.route.params.subscribe({
      next: (params) => {
        const id = params["id"];
        if (!id) {
          this.router.navigate(["/404"], { skipLocationChange: true });
          return;
        }

        this.compService.getComponent(id).subscribe({
          next: (comp) => {
            this.component = comp as any;
            this.setRecursiveParentId(this.component);
            if (this.nameElement?.nativeElement) {
              this.nameElement.nativeElement.innerText = this.component.name;
            }
            console.log("All Components", this.component);
          },
          error: (e) => {
            if (e.status == 404) {
              this.router.navigate(["/404"], { skipLocationChange: true });
            }
            // No comp
            console.error(e);
          },
        });
      },
    });

    this.subscriptions.push(
      this.eventService.widthChanged$.subscribe(({ component }) => {
        this.onWidthChanged({ component });
      })
    );

    this.subscriptions.push(
      this.eventService.textChanged$.subscribe(({ component }) => {
        this.textChanged({ component });
      })
    );

    this.subscriptions.push(
      this.eventService.taskChanged$.subscribe(({ component }) => {
        this.taskChanged({ component });
        console.log("The Task status of the following component just changed:");
        console.log(component);
      })
    );

    this.subscriptions.push(
      this.eventService.taskRepeated$.subscribe(({ component }) => {
        this.taskRepeat({ component });
      })
    );

    this.subscriptions.push(
      this.eventService.enableEditMode$.subscribe(() => {
        this.inEditMode = true;
      })
    );

    this.subscriptions.push(
      this.eventService.deleteComponent$.subscribe({
        next: (value) => {
          this.openDeleteModal(value.component);
        },
      })
    );
  }

  ngOnDestroy() {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  private recursiveDelete(children: Comp[], id: number): Comp[] {
    // map is only to make a copy
    return children
      .map((comp) => {
        return { ...comp };
      })
      .filter((comp) => {
        if ("children" in comp) {
          comp.children = this.recursiveDelete((comp as any).children, id);
        }
        return comp.id !== id;
      });
  }

  private setRecursiveParentId(parent: Comp) {
    if (!(parent as any).children) return;

    (parent as Container).children.forEach((child) => {
      child.parentId = parent.id;
      this.setRecursiveParentId(child);
    });
  }
}
