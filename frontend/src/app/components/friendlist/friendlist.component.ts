import {Component, OnInit} from '@angular/core';
import { UserService } from "../../services/user.service";
import {ActivatedRoute, Router} from "@angular/router";
import {FriendDto, User} from "../../dtos/user";
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'app-friendlist',
  templateUrl: './friendlist.component.html',
  styleUrl: './friendlist.component.scss',
  standalone: false
})
export class FriendlistComponent implements OnInit{

  user: User;
  friends: FriendDto[] = [];
  displayedFriends: FriendDto[] = [];

  constructor(
    public userService: UserService,
    public router: Router,
    public route: ActivatedRoute,
    public authService: AuthService
  ) {
    this.user = authService.getLoggedInUser();
  }

  ngOnInit(): void {
    this.getAllFriends();
  }

  getAllFriends(){
    this.userService.getFriends(this.user, false).subscribe({
      next: friends => {
        this.friends = friends;
        this.displayedFriends = this.friends;

        for (let friend of friends){
          this.userService.getAvatarSrc(friend).subscribe({
            next: (src) => ((document.querySelector("#avatar-" + friend.username) as HTMLImageElement).src = src),
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


  acceptFriend(friend: FriendDto, event: Event){
    event.stopPropagation()
    this.userService.acceptFriendRequest(this.user, friend.username).subscribe({
      next: value => {
        const i = this.friends.indexOf(friend)
        friend.accepted = true;
        this.friends.splice(i, 1, friend);
      }
    })
  }

  deleteFriend(friend: FriendDto, event: Event){

    event.stopPropagation()
    this.userService.deleteFriend(this.user, friend.username).subscribe({
      next: value => {
        const i = this.friends.indexOf(friend)
        this.friends.splice(i,1);
      }
    })

  }



}


