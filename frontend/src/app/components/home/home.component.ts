import { Board, Container, Text, Component as Comp } from '../../dtos/component';
import { ComponentService } from '../../services/component.service';
import { DragService } from '../../interaction-services/drag.service';
import {AuthService} from '../../services/auth.service';
import {ActivatedRoute, Router} from "@angular/router";
import {Component, OnInit, ViewChild} from "@angular/core";
import {EventService} from "../../interaction-services/event.service";



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
    private dragService: DragService,
    private eventService: EventService
  ) { }

  @ViewChild("name") nameElement;

  inEditMode: boolean = false;

  // used for component dragging
  dragging = false;
  draggingContainer: Container | null = null;
  previewContainer: Container | null = null;

  // essentially all boards we are going to show, so all the root boards that have no parent
  component: Container = undefined;




  createBoard() {
    // or child board beneath the lowest child component
    const neighbors = this.component.children
    const row = this.getNextUnusedRow(neighbors)

    const newBoard: Board = {
      children: [],
      column: 1,
      height: 2,
      name: "My New Board",
      type: "board",
      width: 2,
      row: row,
      parentId: this.component.id
    }

    this.compService.createBoard(newBoard).subscribe({
      next : created => neighbors.push(created),
      error : err => console.error(err)
    });

  }

  updateName(){
    const name = this.nameElement.nativeElement.textContent;
    const update = { id: this.component.id, name: name };
    this.compService.updateBoard(update as any).subscribe({
      next: () => window.dispatchEvent(new CustomEvent("board-name-changed", { detail: { id: update.id, name } })),
      error: (e) => console.error(e),
    });

  }
  private getNextUnusedRow(neighbors: Comp[]): number{
    if (neighbors.length === 0){
      return 1;
    }
    const sorted = neighbors.sort((a, b) => b.row - a.row);
    return sorted[0].row + sorted[0].height
  }

  onWidthChanged(event: { component: Comp }) {

    this.compService.updateContainer(event.component as any).subscribe({
      next: () => console.log('Updated successfully'),
      error: err => console.error('Update failed', err)
    });

  }

  titleChanged(event: { component: Comp }) {

    this.compService.updateContainer(event.component as any).subscribe({
      next: () => console.log('Updated successfully'),
      error: err => console.error('Update failed', err)
    });

  }

  textChanged(event: { component: Comp }) {

    // TODO: call service to update text and subscribe!!!!

  }

  createChild(type: string){
    // TODO: add other components
    switch (type){
      case 'board' :
        this.createBoard();
        break;
      default:
        console.error("unsupported type of component")
    }
  }

  deleteBoard(){
    this.compService.deleteComponent(this.component.id).subscribe({
      next: value => {
        window.dispatchEvent(new CustomEvent("board-delete", { detail: { id: this.component.id, name: this.component.name } }))
        this.router.navigate(["/"]);
      }
    })
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
        bottom: row + height
      };

      // Check for collision with any existing board
      const collision = children.some(child => {
        const childArea = {
          left: child.column,
          right: child.column + child.width,
          top: child.row,
          bottom: child.row + child.height
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

  startDragging(data: { component: Comp, event: MouseEvent }) {
    this.dragService.startDragging(data, <Container>this.component);
  }

  onMouseMove(event: MouseEvent) {
    this.dragService.onMouseMove(event, <Container>this.component, (container, targetContainer) => {

    });
  }

  stopDragging(event: MouseEvent) {
    this.dragService.stopDragging(event, <Container>this.component, (container, targetContainer) => {
      console.log("target", targetContainer)
      this.compService.updateContainer({...container, parentId: targetContainer.id} as any).subscribe();
    });
  }



  logOut() {
    this.authService.logoutUser();
    this.router.navigate(["/login"]);
  }

  deleteUser() {}

  ngOnInit() {
    window.addEventListener('mousemove', this.onMouseMove.bind(this));
    window.addEventListener('mouseup', this.stopDragging.bind(this));

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
            this.component = comp as any
            this.component.children.forEach(child => {
              child.parentId = this.component.id
            })
            if (this.nameElement.nativeElement) {
              this.nameElement.nativeElement.innerText = this.component.name
            }
            console.log(this.component)
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



    this.eventService.widthChanged$.subscribe(({ component }) => {
      this.onWidthChanged({component})
    });

    this.eventService.titleChanged$.subscribe(({ component }) => {
      this.titleChanged({component})
    });

    this.eventService.textChanged$.subscribe(({ component }) => {
      // TODO: Handle text change
      console.log("The text of the following component just changed:")
      console.log(component)

    });

    this.eventService.enableEditMode$.subscribe(() => {
      this.inEditMode = true;
    });

    this.eventService.deleteComponent$.subscribe({
      next: value => {
        this.compService.deleteComponent(value.component.id).subscribe({
          next: value1 => this.component.children = this.recursiveDelete(this.component.children, value.component.id),
          error: err => console.error("unable to delete component", value.component.id, "with error", err)
        })

      }
    });

  }


  private recursiveDelete(children: Comp[], id: number): Comp[]{
    // map is only to make a copy
    return children.map(comp => {
      return {...comp}
    }).filter(comp => {
      if ('children' in comp) {
        comp.children = this.recursiveDelete(comp.children, id);
      }
      return comp.id !== id
    })
  }

}
