import { Component, OnInit } from "@angular/core";
import { UntypedFormBuilder, UntypedFormGroup } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { map, switchMap } from "rxjs";
import { User } from "src/app/dtos/user";
import { UserService } from "src/app/services/user.service";

@Component({
  selector: "app-user",
  templateUrl: "./user.component.html",
  styleUrls: ["./user.component.scss"],
  standalone: false,
})
export class UserComponent implements OnInit {
  canManage: boolean = false;
  isOwn: boolean = false;
  isChanged: boolean = false;

  user: User;

  updateForm: UntypedFormGroup;

  constructor(
    private formBuilder: UntypedFormBuilder,
    private userService: UserService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.updateForm = this.formBuilder.group({ displayName: [], email: [] });
  }

  ngOnInit(): void {
    this.route.params
      .pipe(map((params) => this.userService.get(params["name"])))
      .subscribe((user) =>
        user.subscribe({
          next: (u) => {
            this.user = u;
            this.canManage = this.userService.canManage(u);
            this.isOwn = this.userService.isCurrentLoggedIn(u);
          },
          error: (e) => {
            this.router.navigate(["/404"], { skipLocationChange: true });
          },
        })
      );
  }

  onSubmit() {
    console.log("ok");
  }

  onDelete() {
    let msg =
      "This action can't be reversed! Are you sure you want to delete: ";
    if (confirm(msg + this.user.username)) {
      this.userService.delete(this.user).subscribe({
        next: () => {
          this.router.navigate(["/"]);
        },
        error: (e) => {
          console.error(e);
          // send notification
        },
      });
    }
  }
}
