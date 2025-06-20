import { Component, ElementRef, OnInit, ViewChild } from "@angular/core";
import { Router, RouterLink } from "@angular/router";
import { AuthService } from "../../services/auth.service";
import { User } from "../../dtos/user";
import { Group } from "../../dtos/group";
import { NgClass, NgForOf, NgIf } from "@angular/common";
import { GroupService } from "../../services/group.service";

// for delete modal
declare var bootstrap: any;

@Component({
  selector: "app-groupoverview",
  templateUrl: "./groupoverview.component.html",
  styleUrl: "./groupoverview.component.scss",
  standalone: true,
  imports: [RouterLink, NgClass],
})
export class GroupoverviewComponent implements OnInit {
  @ViewChild("modal") modal: ElementRef;

  groups: Group[];
  user: User;

  modalGroup: Group = undefined;
  modalMode: string = "leave";

  constructor(public router: Router, public authService: AuthService, public groupService: GroupService) {
    this.user = authService.getLoggedInUser();
  }

  createGroup() {
    this.groupService.createGroup().subscribe({
      next: (value) => this.groups.push(value),
      error: (err) => console.error("Error on group creation", err),
    });
  }

  leaveGroup(group: Group) {
    const modal = new bootstrap.Modal(this.modal.nativeElement);
    this.modalGroup = group;
    this.modalMode = "leave";
    modal.show();
  }

  deleteGroup(group: Group) {
    const modal = new bootstrap.Modal(this.modal.nativeElement);
    this.modalGroup = group;
    this.modalMode = "delete";
    modal.show();
  }

  modalAction() {
    if (this.modalMode == "delete") {
      console.log("Delete Group");
      this.groupService.deleteGroup(this.modalGroup?.id).subscribe();
    } else {
      console.log("Leave Group");
      this.groupService.leaveGroup(this.modalGroup?.id).subscribe();
    }
    this.groups = this.groups.filter((group) => group.id !== this.modalGroup.id);
  }

  capitalizeFirstLetter(word: string) {
    if (!word) return word;
    return word[0].toUpperCase() + word.slice(1);
  }

  ngOnInit(): void {
    this.groupService.getAllGroups().subscribe({
      next: (value) => (this.groups = value.sort((a, b) => a.name.localeCompare(b.name))),
      error: (err) => console.error("Error fetching groups", err),
    });
  }

  createBoard() {}
}
