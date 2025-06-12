import { Component, OnInit } from "@angular/core";
import { AuthService } from "../../services/auth.service";
import { ComponentService } from "src/app/services/component.service";
import { UserService } from "src/app/services/user.service";
import { ActivatedRoute, Router } from "@angular/router";

import { Board, Component as Comp, isType } from "../../dtos/component";
import { gridVar } from "src/app/dtos/grid";

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

  constructor(
    public router: Router,
    public authService: AuthService,
    public userService: UserService,
    public compService: ComponentService
  ) {
    userService.getAvatarSrc(authService.getLoggedInUser()).subscribe({
      next: (src) => (this.profileImgSrc = src),
      error: (e) => console.error(e),
    });
  }

  public isCurrentlyViewed(item: Comp) {
    return this.router.url.toString().includes(item.id.toString());
  }

  ngOnInit() {
    this.compService.getRoots().subscribe({
      next: (v) => (this.roots = v.filter((c) => isType(c, "board"))),
      error: (e) => console.log(e),
    });

    window.addEventListener("board-name-changed", (event) => {
      const { id, name } = (event as any).detail;
      this.roots.find((i) => i.id == id).name = name;
    });

    window.addEventListener("board-delete", (event) => {
      const { id, name } = (event as any).detail;
      const i = this.roots.findIndex((i) => i.id == id);
      if (i >= 0) this.roots.splice(i, 1);
    });

    window.addEventListener("board-created-homepage", (event) => {
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
}
