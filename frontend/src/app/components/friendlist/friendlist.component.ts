import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import { UserService } from "../../services/user.service";
import {ActivatedRoute, Router} from "@angular/router";
import {FriendDto, User} from "../../dtos/user";
import {AuthService} from "../../services/auth.service";
import {Group} from "../../dtos/group";
import {GroupService} from "../../services/group.service";
import {generateAvatar} from "../user/user.component";

declare var bootstrap: any;

@Component({
  selector: 'app-friendlist',
  templateUrl: './friendlist.component.html',
  styleUrl: './friendlist.component.scss',
  standalone: false
})
export class FriendlistComponent implements OnInit{

  @ViewChild("removeModal") removeModal: ElementRef;

  user: User;
  friends: FriendDto[] = [];
  displayedFriends: FriendDto[] = [];

  forRemoval: FriendDto;

  myGroups: Group[] = [];
  commonGroups: Group[] = [];
  user_pfp: Map<string, string> = new Map<string, string>()

  constructor(
    public userService: UserService,
    public groupService: GroupService,
    public router: Router,
    public route: ActivatedRoute,
    public authService: AuthService
  ) {
    this.user = authService.getLoggedInUser();
  }

  ngOnInit(): void {
    this.getAllFriends();
    this.groupService.getMyGroups().subscribe({
      next: (value) => (this.myGroups = value),
      error: (err) => console.error("My groups error", err),
    });
  }

  getCommonGroups(friend: FriendDto) {
    this.groupService.getCommonGroups(friend.username).subscribe({
      next: value => {
        console.log("common groups:", value)
        this.commonGroups = value;
      },
      error: err => console.error("Failed to fetch common groups", err)
    })
  }

  isCommonGroup(group: Group): boolean {
    return this.commonGroups.map((g) => g.id).includes(group.id);
  }

  changeGroup(group: Group, friend: FriendDto, event?: Event) {
    event.stopPropagation();
    if (this.commonGroups.map((g) => g.id).includes(group.id)) {
      this.groupService.deleteGroupMember(group.id, friend.username).subscribe({
        next: value => {

          this.commonGroups = this.commonGroups.filter((g) => g.id !== group.id)
          console.log(this.commonGroups)
        },
        error: err => console.error("Delete Group Member failed", err)
      })
    } else {
      this.groupService.addGroupMember(group.id, friend.username).subscribe({
        next: value => this.commonGroups.push(group),
        error: err => console.error("Delete Group Member failed", err)
      })
    }
  }

  getAllFriends(){
    this.userService.getFriends(this.user, false).subscribe({
      next: friends => {
        this.friends = friends;
        this.displayedFriends = this.friends;

        for (let friend of friends){
          this.userService.getAvatarSrc(friend).subscribe({
            next: (src) => {
              if (src){
                this.user_pfp.set(friend.username, src);
              } else {
                this.user_pfp.set(friend.username, generateAvatar(friend.username));
              }
            },
            error: (e) => console.error(e),
          });
        }

      }
    })
  }

  allFriends(){
    this.displayedFriends = this.friends
  }
  onlyFriends(){
    this.displayedFriends = this.friends.filter(friend => friend.accepted)
  }

  onlySent(){
    this.displayedFriends = this.friends.filter(friend => !friend.accepted && friend.requesterName === this.user.username)
  }

  onlyReceived(){
    this.displayedFriends = this.friends.filter(friend => !friend.accepted && friend.requesterName === friend.username)
  }

  acceptFriend(friend: FriendDto){
    this.userService.acceptFriendRequest(this.user, friend.username).subscribe({
      next: value => {
        const i = this.friends.indexOf(friend)
        friend.accepted = true;
        this.friends.splice(i, 1, friend);
      }
    })
  }

  openModal(friend: FriendDto) {
    this.forRemoval = friend;
    const modal = new bootstrap.Modal(this.removeModal.nativeElement);
    modal.show();
  }

  removeFriend(friend: FriendDto){
    this.userService.deleteFriend(this.user, friend.username).subscribe({
      next: value => {
        const i = this.friends.indexOf(friend)
        this.friends.splice(i,1);
      }
    })
  }

  protected readonly event = event;
}


