import { Component, OnInit } from "@angular/core";
import { AuthService } from "../../services/auth.service";
import { ComponentService } from "src/app/services/component.service";
import { UserService } from "src/app/services/user.service";
import { ActivatedRoute, Router } from "@angular/router";

import { Board, Component as Comp, ComponentNameTypeMap, ComponentType, isType } from "../../dtos/component";
import { gridVar } from "src/app/dtos/grid";
import { GroupService } from "../../services/group.service";

@Component({
  selector: "app-header",
  templateUrl: "./header.component.html",
  styleUrls: ["./header.component.scss"],
  standalone: false,
})
export class HeaderComponent implements OnInit {
  profileImgSrc: string;
  hoverProfile: boolean = false;

  isVisible: boolean = true;

  roots: Board[] = [];
  sharedRoots: Board[] = [];

  constructor(
    public router: Router,
    public authService: AuthService,
    public userService: UserService,
    public compService: ComponentService,
    public groupService: GroupService
  ) {
  }

  public isCurrentlyViewed(item: Comp) {
    const urlSegments = this.router.url.split("/");
    const currentId = urlSegments[urlSegments.length-1];
    return currentId === item.id.toString()
  }

  ngOnInit() {
    if (!this.authService.isLoggedIn()) return;

    const user = this.authService.getLoggedInUser();
    this.userService.getAvatarSrc(user).subscribe({
      next: (src) => (this.profileImgSrc = src),
      error: (e) => console.error(e),
    });

    this.groupService.getGroupRoots().subscribe({
      next: (v) => {
        this.sharedRoots = v.filter((c) => isType(c, "board"));
        this.compService.getRoots().subscribe({
          next: (v) => {
            this.roots = v.filter((c) => isType(c, "board"));
            this.roots = this.roots.filter((b) => !this.isSharedBoard(b));
          },
          error: (e) => console.error(e),
        });
      },
      error: (e) => console.error("Problem fetching shared Roots", e),
    });

    window.addEventListener("board-name-changed", (event) => {
      const { id, name } = (event as any).detail;
      const root = this.roots.find((i) => i.id == id);
      const sharedRoot = this.sharedRoots.find((i) => i.id == id);

      if (root) root.name = name;
      if (sharedRoot) sharedRoot.name = name;
    });

    window.addEventListener("board-delete", (event) => {
      const { id, name } = (event as any).detail;
      let i = this.roots.findIndex((i) => i.id == id);
      if (i == -1) {
        i = this.sharedRoots.findIndex((i) => i.id == id);
        if (i >= 0) this.sharedRoots.splice(i, 1);
      } else {
        if (i >= 0) this.roots.splice(i, 1);
      }
    });

    window.addEventListener("board-created", (event) => {
      const createdRoot = (event as any).detail;
      this.roots.push(createdRoot);
    });
  }

  logout() {
    this.authService.logoutUser();
    this.router.navigate(["/login"]);
  }

  createBoard() {
    const item: Partial<Board> = { name: "Unnamed", column: 1, row: 1, width: gridVar.columns, height: 4 };
    this.compService.createBoard(item as any).subscribe({
      next: (board) => this.roots.push(board),
      error: (e) => console.error(e),
    });
  }

  isSharedBoard(component: Board) {
    return this.sharedRoots.map((board) => board.id).includes(component.id);
  }
}
