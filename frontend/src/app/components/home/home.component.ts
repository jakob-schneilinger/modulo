import {
  Board,
  Container,
  Text,
  Component as Comp,
  Note,
  Task,
  Calendar,
  isType,
  isContainer,
  CalendarEntry
} from "../../dtos/component";
import { ComponentService } from "../../services/component.service";
import { DragService } from "../../interaction-services/drag.service";
import { AuthService } from "../../services/auth.service";
import { ActivatedRoute, Router } from "@angular/router";
import { Component, OnInit, ViewChild, ElementRef, OnDestroy, ComponentRef } from "@angular/core";
import { EventService } from "../../interaction-services/event.service";
import { Subscription } from "rxjs";
import { gridVar } from "../../dtos/grid";
import { GroupService } from "../../services/group.service";
import { GroupWithPermission } from "../../dtos/group";
import { type ComponentUpdate, WebsocketService } from "../../services/websocket.service";
import { ContainerComponent } from "../comp/containers/container.component";

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
    public groupService: GroupService,
    private dragService: DragService,
    private eventService: EventService,
    private websocketService: WebsocketService
  ) {}

  @ViewChild("name") nameElement: ElementRef;
  @ViewChild("board") set boardElement(comp: ContainerComponent<Board>) {
    if (comp) this._board = comp;
  }
  private _board: ContainerComponent<Board>;
  @ViewChild("deleteModal") deleteModal: ElementRef;
  @ViewChild("selectTemplateModal") templateModal: ElementRef;
  @ViewChild("preview", { static: false }) previewEl!: ElementRef;

  inEditMode: boolean = false;

  readonlyMode: boolean = true;
  isOwner: boolean = false;

  private subscriptions: Subscription[] = [];

  // used for component dragging
  dragging = false;

  // essentially all boards we are going to show, so all the root boards that have no parent
  component: Board = undefined;
  forDeletion: Comp = undefined;

  myGroups: GroupWithPermission[] = [];

  createBoard() {
    const width = Math.floor(gridVar.columns / 4);
    const height = Math.floor(gridVar.columns / 4);
    const { column, row } = this.findFirstFreeSpace(width, height);

    const newBoard: Board = {
      children: [],
      column,
      height,
      name: "My New Board",
      type: "board",
      width,
      row,
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
    const { column, row } = this.findFirstFreeSpace(width, height);

    const newText: Text = {
      column,
      height,
      content: "Add Text here:",
      width,
      type: "text",
      row,
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
    const { column, row } = this.findFirstFreeSpace(width, height);
    this.compService.createImage({ parentId: this.component.id, column, row, width, height }, null).subscribe({
      next: (comp) => this.addChild({ ...comp, sketch: isSketch } as Comp),
      error: (e) => console.error(e),
    });
  }

  createVideo() {
    const width = Math.floor(gridVar.columns / 2);
    const height = Math.floor(gridVar.columns / 4);
    const { column, row } = this.findFirstFreeSpace(width, height);
    this.compService.createVideo({ parentId: this.component.id, column, row, width, height }, null).subscribe({
      next: (comp) => this.addChild(comp),
      error: (e) => console.error(e),
    });
  }

  createCalendar() {
    const width = Math.floor(gridVar.columns / 4);
    const height = Math.floor(gridVar.columns / 4);
    const { column, row } = this.findFirstFreeSpace(width, height);
    this.compService.createCalendar({ parentId: this.component.id, column, row, width, height }).subscribe({
      next: (comp) => this.addChild(comp),
      error: (e) => console.error(e),
    });
  }

  createNote() {
    const neighbors = this.component.children;
    const width = Math.floor(gridVar.columns / 4);
    const height = Math.floor(gridVar.columns / 4);
    const { column, row } = this.findFirstFreeSpace(width, height);

    const createDto: Partial<Note> = {
      type: "note",
      row,
      column,
      width,
      height,
      name: "New Note",
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
    const height = Math.floor(gridVar.columns / 8);
    const { column, row } = this.findFirstFreeSpace(width, height);

    const newTask: Task = {
      children: [],
      column,
      completed: false,
      height,
      name: "New Task",
      row,
      repeating: false,
      startDate: undefined,
      type: "task",
      width,
      parentId: this.component.id,
    };

    this.compService.createTask(newTask).subscribe({
      next: (created) => this.addChild(created),
      error: (err) => console.error(err),
    });
  }

  createTaskFromCalendar(id: number){
    const width = Math.floor(gridVar.columns / 2);
    const height = Math.floor(gridVar.columns / 3);
    const {column, row} = this.findFirstFreeSpace(width, height);

    const newTask: Task = {
      children: [],
      column,
      completed: false,
      height,
      name: "New Task",
      row,
      repeating: false,
      startDate: undefined,
      type: "task",
      width,
      parentId: this.component.id,
    };

    this.compService.createTaskFromCalendar(newTask, id).subscribe({
      next: (created) => this.addChild(created),
      error: (err) => console.error(err),
    });

  }

  createTaskFromCalendarEntry(entry: CalendarEntry){
    const width = Math.floor(gridVar.columns / 4);
    const height = Math.floor(gridVar.columns / 3);
    const {column, row} = this.findFirstFreeSpace(width, height);

    const newTask: Task = {
      children: [],
      column,
      completed: false,
      height,
      name: entry.title,
      row,
      repeating: false,
      startDate: entry.startDate,
      type: "task",
      width,
      parentId: this.component.id,
      endDate: entry.endDate ? entry.endDate : undefined
    };

    this.compService.createTask(newTask).subscribe({
      next: (created) => this.addChild(created),
      error: (err) => console.error(err),
    });
  }

  addChild(comp: Comp) {
    console.log("Child created:", comp)
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
      error: (err) => {
        this.compService.getComponent(event.component.id).subscribe({
          next: (e) => {
            this.findAndPatch(this.component.children, e);
          },
          error: (er) => {
            console.log("this should not happen");
          }
        })
        console.error("Failed to update task", err);
      }
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
      case "video":
        this.createVideo();
        break;
      case "task":
        this.createTask();
        break;
      case "note":
        this.createNote();
        break;
      case "template":
        this.openTemplateModal();
        break;
      case "sketch":
        this.createImage(true);
        break;
      case "calendar":
        this.createCalendar();
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

  private findFirstFreeSpace(width: number, height: number): { column: number; row: number } {
    const children = this.component.children;
    let row = 1;
    while (true) {
      for (let i = 1; i <= gridVar.columns + 1 - width; i++) {
        const newArea = {
          left: i,
          right: i + width,
          top: row,
          bottom: row + height,
        };

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
          return { column: i, row };
        }
      }
      row++;
    }
  }

  startDragging(data: { component: Comp; preview: ElementRef; event: MouseEvent | TouchEvent }) {
    this.dragging = true;
    data.preview = this.previewEl;
    this.dragService.startDragging(data, <Container>this.component);
  }

  onMouseMove(event: MouseEvent | TouchEvent) {
    this.dragService.onMouseMove(event, <Container>this.component);
  }

  stopDragging(event: MouseEvent | TouchEvent) {
    if (!this.dragging) return;
    this.dragging = false;
    this.dragService.stopDragging(event, <Container>this.component, (container, targetComponent) => {
      //container.parentId = targetContainer.id;

      if (targetComponent.type === "calendar") {
        this.compService.addTaskToCalendar(container, targetComponent.id).subscribe({
          next: (value) => {
            this.findAndPatch(this.component.children, value);
            console.log("Added Task to Calendar");
          },
          error: (err) => {
            console.error("Failed  to add Task to Calendar", err);
          },
        })
      } else if(container.type === "task"){
        this.compService.updateTask({ ...container, parentId: targetComponent.id}).subscribe({
          next: (value) => {
            console.log("Drag/Resize success");
          },
          error: (err) => {
            this.compService.getComponent(container.id).subscribe({
                next: (e) => {
                  this.findAndPatch(this.component.children, e);
                },
                error: (er) => {
                  console.log("this should not happen");
            }
            })
            console.error("Failed  to add Task to Calendar", err);
          }
        })
      } else {
        this.compService.updatePosAndSize({ ...container, parentId: targetComponent.id } as any).subscribe({
          next: (value) => {
            console.log("Drag/Resize success");
          },
          error: (err) => {
            console.error("Error on dragging", err);
          },
        });
      }
    });
  }

  getCompDeleteMsg(comp: Comp<any>): string {
    return `Are you sure you want to delete the component ${(comp as any)?.name || `of type ${comp.type}`}?`;
  }

  onReadChange(groupWithPermission: GroupWithPermission): void {
    if (!groupWithPermission.permission.read && groupWithPermission.permission.write) {
      groupWithPermission.permission.write = false;
    }
    this.sendUpdate(groupWithPermission);
  }

  onWriteChange(groupWithPermission: GroupWithPermission): void {
    if (groupWithPermission.permission.write) {
      groupWithPermission.permission.read = true;
    }
    this.sendUpdate(groupWithPermission);
  }

  private sendUpdate(groupWithPermission: GroupWithPermission): void {
    // Replace with actual update logic
    const permissions = groupWithPermission.permission;
    if (!permissions.read) {
      this.groupService.removeGroupFromBoard(groupWithPermission.groupId, this.component.id).subscribe();
    } else {
      this.groupService.addGroupToBoard(groupWithPermission).subscribe();
    }
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

  openTemplateModal() {
    const modal = new bootstrap.Modal(this.templateModal.nativeElement);
    modal.show();
  }

  onTemplateSelected(template: Board) {
    const { column, row } = this.findFirstFreeSpace(template.children[0].width, template.children[0].height);
    this.compService
      .createComponentFromTemplate({
        height: 0,
        type: undefined,
        width: 0,
        templateRootId: template.id,
        column: column,
        row: row,
        parentId: this.component.id
      })
      .subscribe({
        next: (created) => {
          console.log("Created: ", created);
          const modal = bootstrap.Modal.getInstance(this.templateModal.nativeElement);
          modal.hide();
        },
        error: (err) => console.error(err),
      });
  }

  logOut() {
    this.authService.logoutUser();
    this.router.navigate(["/login"]);
  }

  minAllowedDepth = 2;
  maxAllowedDepth = 8;
  currentDepth: number;

  incrementDepth() {
    if (this.currentDepth < this.maxAllowedDepth) {
      this.currentDepth++;
      this.component.depth++;
      this.compService.changeBoardDepth(this.component.id, this.currentDepth).subscribe();
    }
  }

  decrementDepth() {
    if (this.currentDepth > this.minAllowedDepth && this.getContainerDepth(this.component) < this.currentDepth) {
      this.currentDepth--;
      this.component.depth--;
      this.compService.changeBoardDepth(this.component.id, this.currentDepth).subscribe();
    } else {
      console.error("Can't decrease board depth, check your board!");
    }
  }

  preventInvalidInput(event: KeyboardEvent) {
    if (["+", "-", ".", "e", "E", ",", " "].includes(event.key)) {
      event.preventDefault();
    }
  }

  sanitizeDepth() {
    const containerDepth = this.getContainerDepth(this.component);
    if (typeof this.currentDepth !== "number" || isNaN(this.component.depth)) {
      this.currentDepth = containerDepth;
    } else {
      if (this.currentDepth < this.getContainerDepth(this.component)) {
        this.currentDepth = containerDepth;
      } else if (this.component.depth < this.minAllowedDepth) {
        this.component.depth = this.minAllowedDepth;
      } else if (this.component.depth > this.maxAllowedDepth) {
        this.component.depth = this.maxAllowedDepth;
      }
    }
    this.compService.changeBoardDepth(this.component.id, this.component.depth).subscribe();
  }

  private getContainerDepth(component: Comp): number {
    if (!isContainer(component) || !component.children?.length) {
      return 0;
    }

    return 1 + Math.max(...component.children.map((child) => this.getContainerDepth(child)));
  }

  ngOnInit() {
    window.addEventListener("mousemove", this.onMouseMove.bind(this));
    window.addEventListener("mouseup", this.stopDragging.bind(this));

    window.addEventListener("touchmove", this.onMouseMove.bind(this));
    window.addEventListener("touchend", this.stopDragging.bind(this));

    // for a user fetch his root boards

    this.route.params.subscribe({
      next: (params) => {
        this.inEditMode = false;
        const id = params["id"];
        if (!id) {
          this.router.navigate(["/404"], { skipLocationChange: true });
          return;
        }

        this.compService.getComponent(id).subscribe({
          next: (comp) => {
            this.component = comp as any;
            this.currentDepth = (comp as Board)?.depth ?? 5;

            this.groupService.getBoardPermission(this.component.id).subscribe({
              next: (value) => {
                console.log("Permission Value:", value);
                if (value == null) {
                  this.isOwner = true;
                  this.readonlyMode = false;

                  this.groupService.getBoardGroups(this.component?.id).subscribe({
                    next: (value) => {
                      this.myGroups = [];
                      for (let i = 0; i < value.length; i++) {
                        if (value[i].permission == null) {
                          this.myGroups.push({ ...value[i], permission: { read: false, write: false } });
                        } else {
                          this.myGroups.push(value[i]);
                        }
                      }
                    },
                    error: (err) => console.error(err),
                  });
                } else {
                  this.isOwner = false;
                  this.readonlyMode = !value;
                }
              },
              error: (err) => console.error("Fetch Permission Error:", err),
            });

            this.websocketService.connect().then(() => {
              this.websocketService.subscribe(this.component?.id, (update) => {
                this.syncUpdate(update);
              });
            });

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
      this.eventService.calendarToTask$.subscribe(({ entry }) => {
        this.createTaskFromCalendarEntry(entry);
      })
    )

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

  syncUpdate(update: ComponentUpdate) {
    console.log("Update:", update);
    if (this.component.id == update.selfId) {
      // Updates on root
      switch (update.type) {
        case "changed":
          // update own fields like name
          this.compService.getComponent(update.selfId).subscribe({
            next: (comp) => this._board.setData(comp),
          });
          break;
        case "deleted":
          // Alert that this board has been deleted and send to home
          // alert("Board has been deleted!");
          window.dispatchEvent(
            new CustomEvent("board-delete", { detail: { id: this.component.id, name: this.component.name } })
          );
          this.router.navigate(["/"]);
          break;
      }
    } else {
      // Updates within this component
      switch (update.type) {
        case "changed":
          // find component and reload it
          this.compService.getComponent(update.selfId).subscribe({
            next: (comp) => {
              // has parent been changed?
              console.log("Update12:", update);
              if (this._board.findChild(comp.id)?.parentId == comp.parentId) {
                // no -> simply update data
                if (!this._board.updateData(update.selfId, comp)) {
                  // or create new child
                  this._board.insert(comp.parentId, comp);
                }
              } else {
                console.log("Update123:", update);
                // yes
                // *move* the html element to the new parent
                // also move the comp ref
                // or simply for now: (TODO: this is not optimal, since we create new elements)
                this._board.remove(update.selfId);
                this._board.insert(comp.parentId, comp);
              }
            },
          });
          break;
        case "deleted":
          // find component and remove it
          this._board.remove(update.selfId);
          break;
      }
    }
  }

  ngOnDestroy() {
    this.websocketService.disconnect();
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

  trackByGroupId(index: number, item: GroupWithPermission): number {
    return item.groupId;
  }
}
