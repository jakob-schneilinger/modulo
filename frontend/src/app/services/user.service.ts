import { Injectable } from "@angular/core";
import { Observable, of, tap } from "rxjs";
import { User } from "../dtos/user";
import { AuthService } from "./auth.service";
import { HttpClient } from "@angular/common/http";
import { Globals } from "../global/globals";

@Injectable({
  providedIn: "root",
})
export class UserService {
  private userBaseUri: string = this.globals.backendUri + "/user";

  constructor(
    private httpClient: HttpClient,
    private globals: Globals,
    private authService: AuthService
  ) {}

  isCurrentLoggedIn(user: User) {
    return this.authService.getLoggedInUser().username == user.username;
  }

  canManage(user: User) {
    return this.isCurrentLoggedIn(user);
  }

  get(username: String): Observable<User> {
    if (username == "me") return of(this.authService.getLoggedInUser());

    return this.httpClient.get<User>(this.userBaseUri + "/" + username);
  }

  updateDisplayName(displayName: String): Observable<null> {
    throw "not implemented!";
  }

  updatePassword(oldPassword: String, newPassword: String): Observable<null> {
    throw "not implemented!";
  }

  delete(user: User) {
    return this.httpClient
      .delete<void>(`${this.userBaseUri}/${user.username}`)
      .pipe(
        tap(() => this.isCurrentLoggedIn(user) && this.authService.logoutUser())
      );
  }
}
