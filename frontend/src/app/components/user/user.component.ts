import { Component, OnInit } from "@angular/core";
import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { map, switchMap } from "rxjs";
import { User, UserUpdateDto } from "src/app/dtos/user";
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
  selectedAvatar: File;
  passwordChangeRequested: boolean = false;
  removeAvatarOnSave: boolean = false;
  error = null;

  get isChanged() {
    return this.displayNameChanged || this.emailChanged || this.avatarChanged || this.passwordChanged;
  }

  get displayNameChanged() {
    return this.updateForm.controls.displayName.value != this.user.displayName;
  }

  get emailChanged() {
    return this.updateForm.controls.email.value != this.user.email;
  }

  get passwordChanged() {
    return !!this.updateForm.controls.password.value;
  }

  get avatarChanged() {
    return !!this.selectedAvatar || this.removeAvatarOnSave;
  }

  user: User;

  updateForm: UntypedFormGroup;

  constructor(
    private formBuilder: UntypedFormBuilder,
    private userService: UserService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.updateForm = this.formBuilder.group({
      displayName: [],
      email: [],
      password: [],
      passwordRepeat: [],
    });
  }

  ngOnInit(): void {
    this.updateForm.reset();
    this.passwordChangeRequested = false;
    this.selectedAvatar = null;
    this.removeAvatarOnSave = false;

    this.route.params.pipe(map((params) => this.userService.get(params["name"]))).subscribe((user) =>
      user.subscribe({
        next: (u) => {
          this.user = u;
          this.canManage = this.userService.canManage(u);
          this.isOwn = this.userService.isCurrentLoggedIn(u);

          this.updateForm.controls.displayName.setValue(this.user.displayName);
          this.updateForm.controls.email.setValue(this.user.email);

          let img: HTMLImageElement = document.querySelector("#avatar");
          img.src = "";

          this.userService.getAvatarSrc(u).subscribe({
            next: (baseUri) => {
              if (baseUri) img.src = baseUri;
              else {
                img.src = generateAvatar(this.user.username);
                img.style.imageRendering = "pixelated";
              }
            },
            error: (e) => {
              this.handleError(e);
            },
          });
        },
        error: (e) => {
          this.router.navigate(["/404"], { skipLocationChange: true });
        },
      })
    );
  }

  passwordsValid() {
    if (this.updateForm.controls.password.value != this.updateForm.controls.passwordRepeat.value) {
      this.updateForm.controls.passwordRepeat.setErrors({ noMatch: true });
      return false;
    }
    return true;
  }

  onSubmit() {
    if (!this.updateForm.valid) {
      console.log("form invalid!", this.updateForm.errors);
      console.log(this.updateForm.controls.email.errors);

      // TODO: show errors
      return;
    }
    const { displayName, email, password } = this.updateForm.controls;
    const update: UserUpdateDto = {};
    if (this.displayNameChanged) update.displayName = displayName.value;
    if (this.emailChanged) update.email = email.value;
    if (this.passwordChanged) update.password = password.value;

    const promises = [];
    if (this.removeAvatarOnSave) promises.push(this.removeAvatar());
    else if (this.avatarChanged) promises.push(this.uploadAvatar(this.selectedAvatar));

    promises.push(
      new Promise<void>((res, rej) => {
        if (!update.displayName && !update.email && !update.password) {
          res();
          return;
        }

        this.userService.update(this.user, update).subscribe({
          next: () => res(),
          error: (e) => rej(e),
        });
      })
    );

    Promise.all(promises)
      .then(() => this.ngOnInit())
      .catch(this.handleError);
  }

  onDelete() {
    const msg = "This action can't be reversed! Are you sure you want to delete: ";
    if (confirm(msg + this.user.username)) {
      this.userService.delete(this.user).subscribe({
        next: () => {
          this.router.navigate(["/"]);
        },
        error: (e) => this.handleError(e),
      });
    }
  }

  changeAvatar() {
    this.removeAvatarOnSave = false;
    let input = document.createElement("input");
    input.type = "file";
    input.click();

    input.addEventListener("change", () => {
      const file = input.files.item(0);

      if (file.size > 1 * 1000 * 1000) {
        console.warn("Avatar bigger than 1mb");
        input.value = "";
        return;
      }

      const reader = new FileReader();
      reader.onloadend = function () {
        let img: HTMLImageElement = document.querySelector("#avatar");
        img.src = reader.result.toString();
      };
      if (file) reader.readAsDataURL(file);

      this.selectedAvatar = file;
    });
  }

  onRemoveAvatar() {
    this.removeAvatarOnSave = true;
    this.selectedAvatar = null;

    let img: HTMLImageElement = document.querySelector("#avatar");
    img.src = generateAvatar(this.user.username);
    img.style.imageRendering = "pixelated";
  }

  uploadAvatar(file: File) {
    return new Promise<void>((res, rej) => {
      this.userService.uploadAvatar(this.user, file).subscribe({
        next: () => res(),
        error: (e) => rej(e),
      });
    });
  }

  removeAvatar() {
    return new Promise<void>((res, rej) => {
      this.userService.removeAvatar(this.user).subscribe({
        next: () => res(),
        error: (e) => rej(e),
      });
    });
  }

  handleError(e: any) {
    console.error(e);

    if (e.error instanceof ErrorEvent) {
      this.error = e.error.message;
    } else {
      this.error = `Error (${e.status}): ${e.error}`;
    }
  }

  vanishError() {
    this.error = null;
  }
}

const colors = [
  [220, 100, 100],
  [220, 120, 120],
  [200, 100, 100],
];
const canvas = document.createElement("canvas");
canvas.width = 10;
canvas.height = 10;
function generateAvatar(seed: string): string {
  const ctx = canvas.getContext("2d");
  ctx.clearRect(0, 0, 10, 10);
  const data = ctx.getImageData(0, 0, 10, 10);
  const set = (x, y, c) => {
    data.data[4 * (y * 10 + x)] = colors[c][0];
    data.data[4 * (y * 10 + x) + 1] = colors[c][1];
    data.data[4 * (y * 10 + x) + 2] = colors[c][2];
    data.data[4 * (y * 10 + x) + 3] = 255;
  };

  let a = 0;
  for (const c of seed) a += c.charCodeAt(0);
  (a %= 22), (a += 20);
  let c = a % 3;
  for (let i = 0, r = 0; i < a; i++) {
    let x = seed.charCodeAt(r % seed.length);
    let y = seed.charCodeAt((r / 2) % seed.length);
    (x %= 4), (y %= 8), (r += x + i);
    set(x + 1, y + 1, c);
    set(8 - x, y + 1, c);
  }
  ctx.putImageData(data, 0, 0);
  return canvas.toDataURL();
}
