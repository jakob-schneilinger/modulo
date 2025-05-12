import { Component, OnInit } from "@angular/core";
import {
  UntypedFormBuilder,
  UntypedFormGroup,
  Validators,
} from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { UserCreateDto } from "src/app/dtos/auth";
import { AuthService } from "src/app/services/auth.service";

@Component({
  selector: "app-create",
  templateUrl: "./create.component.html",
  styleUrls: ["./create.component.scss", "../auth.scss"],
  standalone: false,
})
export class CreateComponent implements OnInit {
  submitted: boolean = false;
  createForm: UntypedFormGroup;

  error = false;
  errorMessage = "";

  constructor(
    private formBuilder: UntypedFormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.createForm = this.formBuilder.group({
      username: ["", [Validators.required, Validators.maxLength(16)]],
      displayName: [],
      email: ["", Validators.required],
      password: ["", [Validators.required, Validators.minLength(8)]],
    });
  }

  onUsernameInput(ev: Event) {
    if (this.createForm.controls.displayName.untouched) {
      this.createForm.controls.displayName.setValue(
        this.createForm.controls.username.value
      );
    }
  }

  onSubmit() {
    this.submitted = true;
    if (!this.createForm.valid) {
      console.log("invalid input!");
      return;
    }

    const { username, displayName, email, password } = this.createForm.controls;
    const dto: UserCreateDto = {
      username: username.value,
      displayName: displayName.value,
      email: email.value,
      password: password.value,
    };
    this.authService.createUser(dto).subscribe({
      next: () => {
        console.log("Successfully created user");
        this.router.navigate(["/users/me"]);
      },
      error: (err) => {
        console.log("Could not log in due to:");
        console.log(err);
        this.error = true;
        if (typeof err.error === "object") {
          this.errorMessage = err.error.detail;
        } else {
          this.errorMessage = err.error;
        }
      },
    });
  }

  ngOnInit(): void {}
}
