import { Component, ElementRef, OnInit, ViewChild } from "@angular/core";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { Group, GroupDetail } from "../../../dtos/group";
import { GroupService } from "../../../services/group.service";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { AuthService } from "../../../services/auth.service";
import { User } from "../../../dtos/user";
import { NgIf } from "@angular/common";

// for delete modal
declare var bootstrap: any;

@Component({
  selector: "app-groupdetail",
  templateUrl: "./groupdetail.component.html",
  styleUrl: "./groupdetail.component.scss",
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, RouterLink, NgIf],
})
export class GroupdetailComponent implements OnInit {
  @ViewChild("modal") modal: ElementRef;

  groupDetail: GroupDetail = { name: "Group" };
  user: User;
  modalMode: string = "delete";
  modalMember: User;

  constructor(
    public router: Router,
    private route: ActivatedRoute,
    public authService: AuthService,
    public groupService: GroupService
  ) {
    this.user = authService.getLoggedInUser();
  }

  updateGroupName() {
    const update: Group = { ...this.groupDetail };
    this.groupService.updateGroupName(update).subscribe({
      next: () => console.log("Group renamed!"),
      error: (e) => console.error(e),
    });
  }

  leaveGroup() {
    const modal = new bootstrap.Modal(this.modal.nativeElement);
    this.modalMode = "Leave Group";
    modal.show();
  }

  deleteGroup() {
    const modal = new bootstrap.Modal(this.modal.nativeElement);
    this.modalMode = "Delete Group";
    modal.show();
  }

  removeMember(user: User) {
    const modal = new bootstrap.Modal(this.modal.nativeElement);
    this.modalMember = user;
    this.modalMode = "Remove Member";
    modal.show();
  }

  setOwner(user: User) {
    const modal = new bootstrap.Modal(this.modal.nativeElement);
    this.modalMember = user;
    this.modalMode = "Set Owner";
    modal.show();
  }

  modalAction() {
    switch (this.modalMode) {
      case "Leave Group":
        console.log("Leave Group");
        this.groupService.leaveGroup(this.groupDetail.id).subscribe({
          next: (value) => this.router.navigate(["/group"]),
          error: (err) => console.log("Leave Group Error", err),
        });
        break;
      case "Delete Group":
        console.log("Delete Group");
        this.groupService.deleteGroup(this.groupDetail.id).subscribe({
          next: (value) => this.router.navigate(["/group"]),
          error: (err) => console.log("Delete Group Error", err),
        });
        break;
      case "Remove Member":
        this.groupService.deleteGroupMember(this.groupDetail.id, this.modalMember.username).subscribe({
          next: () =>
            (this.groupDetail.members = this.groupDetail.members.filter(
              (m) => m.username !== this.modalMember.username
            )),
          error: (err) => console.error("Failed to remove member", err),
        });
        break;
      case "Set Owner":
        this.groupService.setGroupOwner(this.groupDetail.id, this.modalMember.username).subscribe({
          next: (updated) => (this.groupDetail.owner = updated.owner),
          error: (err) => console.error("Failed to set new owner", err),
        });
        break;
    }
  }

  modalTitle() {
    switch (this.modalMode) {
      case "Leave Group":
        return 'Leave Group "' + this.groupDetail.name + '"?';
      case "Delete Group":
        return 'Delete Group "' + this.groupDetail.name + '"?';
      case "Remove Member":
        return 'Remove Member "' + this.modalMember.displayName + '"?';
      case "Set Owner":
        return 'Set Owner to "' + this.modalMember.displayName + '"?';
    }
  }

  isOwner() {
    return this.user?.username === this.groupDetail?.owner?.username;
  }

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      const id = params["id"];
      this.groupService.getGroup(id).subscribe({
        next: (value) => {
          let sortedUsers = value.members.sort((a, b) => a.displayName.localeCompare(b.displayName));
          sortedUsers = sortedUsers.filter((u) => u.username !== this.user.username);
          sortedUsers.push(this.user);
          value.members = sortedUsers;
          this.groupDetail = value;
        },
        error: (err) => console.error("Error fetching group details", err),
      });
    });
  }
}
