import { Component, OnInit } from "@angular/core";
import { AuthService } from "../../services/auth.service";
import { ComponentService } from "src/app/services/component.service";
import { UserService } from "src/app/services/user.service";
import { Router } from "@angular/router";

import { Board, BoardCreate, Component as Comp } from "../../dtos/component";

@Component({
  selector: "app-header",
  templateUrl: "./header.component.html",
  styleUrls: ["./header.component.scss"],
  standalone: false,
})
export class HeaderComponent implements OnInit {
  profileImgSrc: string;
  hoverProfile: boolean = false;

  roots: Comp[];

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

  ngOnInit() {
    this.compService.getRoots().subscribe({
      next: (v) => (this.roots = v),
      error: (e) => console.log(e),
    });

    window.addEventListener("board-name-changed", (event) => {
      const { id, name } = (event as any).detail;
      this.roots.find((i) => i.id == id).name = name;
    });
  }

  logout() {
    this.authService.logoutUser();
    this.router.navigate(["/login"]);
  }

  createBoard() {
    const item: BoardCreate = { name: "Unnamed" };
    this.compService.createBoard(item as any).subscribe({
      next: (board) => this.roots.push(board), //
      error: (e) => console.error(e),
    });
  }
}
