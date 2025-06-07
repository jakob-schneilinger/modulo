import { Injectable } from "@angular/core";
import { map, Observable, of, tap } from "rxjs";
import {FriendDto, User, UserUpdateDto} from "../dtos/user";
import { AuthService } from "./auth.service";
import { HttpClient, HttpParams } from "@angular/common/http";
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
    if (username == "me")
      username = this.authService.getLoggedInUser().username;

    return this.httpClient.get<User>(this.userBaseUri + "/" + username);
  }

  update(user: User, updateDto: UserUpdateDto): Observable<void> {
    let url = `${this.userBaseUri}/${user.username}`;
    return this.httpClient.patch<void>(url, updateDto);
  }

  delete(user: User) {
    return this.httpClient
      .delete<void>(`${this.userBaseUri}/${user.username}`)
      .pipe(
        tap(() => this.isCurrentLoggedIn(user) && this.authService.logoutUser())
      );
  }

  getAvatarSrc(user: User): Observable<string> {
    const url = `${this.userBaseUri}/${user.username}/avatar`;
    return this.httpClient
      .get(url, { responseType: "blob" })
      .pipe(
        map((blob?: Blob) => (!blob ? "" : window.URL.createObjectURL(blob)))
      );
  }

  uploadAvatar(user: User, file: File) {
    const form = new FormData();
    form.append("file", file, "Test");
    const url = `${this.userBaseUri}/${user.username}/avatar`;
    return this.httpClient.post<void>(url, form);
  }

  removeAvatar(user: User) {
    const url = `${this.userBaseUri}/${user.username}/avatar`;
    return this.httpClient.delete<void>(url);
  }

  getFriends(user: User, onlyFriends: boolean) {
    const url = `${this.userBaseUri}/${user.username}/friends`;
    let params = new HttpParams();
    params = params.set("onlyfriends", onlyFriends)

    return this.httpClient.get<FriendDto[]>(url, {params: params})
  }

  sendFriendRequest(user: User, friendName: string) {
    const url = `${this.userBaseUri}/${user.username}/friends`;
    let params = new HttpParams();
    params = params.set("friendName", friendName)

    return this.httpClient.post<void>(url, null, {params: params})
  }

  acceptFriendRequest(user: User, friendName: string) {
    const url = `${this.userBaseUri}/${user.username}/friends`;
    let params = new HttpParams()
    params = params.set("friendName", friendName)

    return this.httpClient.put<void>(url, null, {params})
  }

  deleteFriend(user: User, friendName: string) {
    const url = `${this.userBaseUri}/${user.username}/friends`;
    let params = new HttpParams();
    params = params.set("friendName", friendName)

    return this.httpClient.delete<void>(url, {params: params})
  }


  isFriend(user: User, friendName: string) {
    const url = `${this.userBaseUri}/${user.username}/friends/isfriend`;
    let params = new HttpParams();
    params = params.set("friendName", friendName)

    return this.httpClient.get<boolean>(url, {params: params})
  }

  getFriend(user: User, friendName: string) {
    const url = `${this.userBaseUri}/${user.username}/friends/friend`;
    let params = new HttpParams();
    params = params.set("friendName", friendName)

    return this.httpClient.get<FriendDto>(url, {params: params})
  }

}
