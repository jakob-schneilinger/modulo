import { Component, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { ComponentService } from "../../services/component.service";
import { AuthService } from "../../services/auth.service";
import { Board, Component as Comp, isType } from "../../dtos/component";
import { User } from "../../dtos/user";

@Component({
  selector: "app-homepage",
  templateUrl: "./homepage.component.html",
  styleUrl: "./homepage.component.scss",
  standalone: false,
})
export class HomepageComponent implements OnInit {
  roots: Board[] = [];
  user: User;

  constructor(public router: Router, public authService: AuthService, public compService: ComponentService) {
    this.user = authService.getLoggedInUser();
  }
  ngOnInit(): void {
    this.compService.getRoots().subscribe({
      next: (v) => (this.roots = v.filter((c) => isType(c, "board"))),
      error: (e) => console.log(e),
    });

    window.addEventListener("board-created-header", (event) => {
      const createdRoot = (event as any).detail;
      this.roots.push(createdRoot);
    });
  }

  createBoard() {
    const item: Board = { name: "Unnamed" } as any;
    this.compService.createBoard(item).subscribe({
      next: (board) => {
        this.roots.push(board);
        window.dispatchEvent(new CustomEvent("board-created-homepage", { detail: board }));
      },
      error: (e) => console.error(e),
    });
  }

  navigateToBoard(board: Comp) {
    this.router.navigate(["/component", board.id]);
  }

  get getDisplayname() {
    if (this.user.displayName) {
      return ", " + this.user.displayName;
    } else return "";
  }
}
