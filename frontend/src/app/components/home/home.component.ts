import { Board, Container, Text, Component as Comp } from "../../dtos/component";
import { ComponentService } from "../../services/component.service";
import { DragService } from "../../interaction-services/drag.service";
import { AuthService } from "../../services/auth.service";
import { ActivatedRoute, Router } from "@angular/router";
import { Component, OnInit, ViewChild } from "@angular/core";

// Board test data
/* const text: Text = {
  title: "Text-Box",
  column: 1,
  row: 1,
  id: 500,
  name: "My Text",
  owner_id: 0,
  type: "text",
  width: 5,
  height: 1,
  text: "Hello World",
};

const containers: Container = {
  id: 0,
  children: [
    {
      id: 1,
      children: [
        {
          id: 11,
          children: [],
          name: "My first Child",
          owner_id: 0,
          type: "board",
          width: 3,
          height: 2,
          column: 1,
          row: 1,
        },
        {
          id: 12,
          children: [],
          name: "My second Child",
          owner_id: 0,
          type: "board",
          width: 3,
          height: 1,
          column: 4,
          row: 1,
        },
      ],
      name: "My first Board",
      owner_id: 0,
      type: "board",
      width: 6,
      height: 3,
      column: 1,
      row: 1,
    },
    {
      id: 2,
      children: [],
      name: "My second Board",
      owner_id: 0,
      type: "board",
      width: 2,
      height: 2,
      column: 7,
      row: 1,
    },
    {
      id: 3,
      children: [],
      name: "My second Note",
      owner_id: 0,
      type: "note",
      width: 4,
      height: 1,
      column: 1,
      row: 4,
    },
    {
      id: 4,
      children: [this.text],
      name: "My first Note",
      owner_id: 0,
      type: "note",
      width: 4,
      height: 2,
      column: 5,
      row: 4,
    },
    {
      id: 5,
      children: [],
      name: "My first Task",
      owner_id: 0,
      type: "task",
      width: 2,
      height: 1,
      column: 3,
      row: 5,
    },
  ],
  name: "Home",
  owner_id: 0,
  type: "board",
  width: 0,
  height: 0,
  column: 0,
  row: 0,
}; */

@Component({
  selector: "app-home",
  templateUrl: "./home.component.html",
  styleUrls: ["./home.component.scss"],
  standalone: false,
})
export class HomeComponent implements OnInit {
  constructor(
    public authService: AuthService,
    public router: Router,
    public route: ActivatedRoute,
    public compService: ComponentService,
    private dragService: DragService
  ) {}

  @ViewChild("name") nameElement;

  inEditMode: boolean = false;

  // used for component dragging
  dragging = false;
  draggingContainer: Container | null = null;
  previewContainer: Container | null = null;

  // components: Component[] = []; //TODO soll aus service kommen
  // boards: Board[] = [];         //TODO soll aus service kommen

  component: Comp;

  createComponent() {
    // TODO: create comp with certain type
  }

  updateName() {
    const name = this.nameElement.nativeElement.textContent;
    const update = { id: this.component.id, name: name };
    this.compService.updateBoard(update as any).subscribe({
      next: () => window.dispatchEvent(new CustomEvent("board-name-changed", { detail: { id: update.id, name } })),
      error: (e) => console.error(e),
    });
  }

  /* createBoard() {
    const children = this.containers.children;

    // Sort by row and column to process top-left to bottom-right
    children.sort((a, b) => {
      if (a.row === b.row) return a.column - b.column;
      return a.row - b.row;
    });

    const row = this.findFirstFreeRow(2, 2);

    const newBoard: Board = {
      children: [],
      column: 1,
      height: 2,
      name: "My New Board",
      type: "board",
      width: 2,
      row: row,
    };

    this.containers.children.push(newBoard);

    this.compService.createBoard(newBoard, null);
  } */

  onWidthChanged(event: { component: Comp }) {
    const update: Comp = { ...event.component, type: "board" };
    /*
    TODO: add when update implemented

    this.compService.updateBoard(update).subscribe({
      next: () => console.log('Updated successfully'),
      error: err => console.error('Update failed', err)
    });
     */
  }

  /* private findFirstFreeRow(width: number, height: number, column: number = 1): number {
    const children = this.containers.children;

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
  } */

  startDragging(data: { component: Comp; event: MouseEvent }) {
    //this.dragService.startDragging(data, this.containers);
  }

  onMouseMove(event: MouseEvent) {
    /* this.dragService.onMouseMove(event, this.containers, (container, targetContainer) => {
      // TODO: Update Component
    }); */
  }

  stopDragging(event: MouseEvent) {
    /* this.dragService.stopDragging(event, this.containers, (container, targetContainer) => {
      // TODO: Backend update (uncomment when implemented)
      // this.compService.updateContainer(container).subscribe();
    }); */
  }

  logOut() {
    this.authService.logoutUser();
    this.router.navigate(["/login"]);
  }

  deleteUser() {}

  ngOnInit() {
    window.addEventListener("mousemove", this.onMouseMove.bind(this));
    window.addEventListener("mouseup", this.stopDragging.bind(this));

    this.route.params.subscribe({
      next: (params) => {
        const id = params["id"];
        if (!id) {
          this.router.navigate(["/404"], { skipLocationChange: true });
          return;
        }

        this.compService.getComponent(id).subscribe({
          next: (comp) => (this.component = comp),
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
  }
}
