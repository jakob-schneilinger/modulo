import {Component, Input, OnInit} from "@angular/core";
import { Board } from "../../../../dtos/items";
import { ActivatedRoute, Router } from "@angular/router";

@Component({
  selector: "app-board-component",
  templateUrl: "./board.component.html",
  styleUrls: ["./board.component.scss"],
  standalone: true,
})
export class BoardComponent implements OnInit {

  constructor(
    private router: Router,
    private route: ActivatedRoute
  ) { }

  // TODO: geht the board from parent
  // @Input() board: Board;

  board: Board = {
    children: [],
    name: "My first Board",
    owner_id: 0,
    type: "board",
    width: 2
  }



  ngOnInit(): void {
  }
}
