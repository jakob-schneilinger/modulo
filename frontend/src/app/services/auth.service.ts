import { Injectable } from "@angular/core";
import { UserCreateDto, UserLoginDto } from "../dtos/auth";
import { Observable } from "rxjs";
import { HttpClient } from "@angular/common/http";
import { tap, map } from "rxjs/operators";
import { jwtDecode } from "jwt-decode";
import { Globals } from "../global/globals";

@Injectable({
  providedIn: "root",
})
export class AuthService {
  private authBaseUri: string = this.globals.backendUri + "/authentication";
  private userBaseUri: string = this.globals.backendUri + "/user";

  constructor(private httpClient: HttpClient, private globals: Globals) {}

  /**
   * Login in the user. If it was successful, a valid JWT token will be stored
   *
   * @param loginDto User data
   */
  loginUser(loginDto: UserLoginDto): Observable<string> {
    return this.httpClient
      .post<{ token: string }>(this.authBaseUri, loginDto)
      .pipe(map((res) => res.token))
      .pipe(tap((token) => this.setToken(token)));
  }

  /**
   * Creates an user.
   *
   * @param createDto User data
   */
  createUser(createDto: UserCreateDto): Observable<any> {
    return this.httpClient
      .post<{ token: string }>(this.userBaseUri + "/register", createDto)
      .pipe(map((res) => res.token))
      .pipe(tap((token) => this.setToken(token)));
  }

  /**
   * Check if a valid JWT token is saved in the localStorage
   */
  isLoggedIn() {
    return (
      !!this.getToken() &&
      this.getTokenExpirationDate(this.getToken()).valueOf() >
        new Date().valueOf()
    );
  }

  logoutUser() {
    console.log("Logout");
    localStorage.removeItem("authToken");
  }

  getToken() {
    return localStorage.getItem("authToken");
  }

  /**
   * Returns the user role based on the current token
   */
  getUserRole() {
    if (this.getToken() != null) {
      const decoded: any = jwtDecode(this.getToken());
      const authInfo: string[] = decoded.rol;
      if (authInfo.includes("ROLE_ADMIN")) {
        return "ADMIN";
      } else if (authInfo.includes("ROLE_USER")) {
        return "USER";
      }
    }
    return "UNDEFINED";
  }

  private setToken(authResponse: string) {
    localStorage.setItem("authToken", authResponse);
  }

  private getTokenExpirationDate(token: string): Date {
    const decoded: any = jwtDecode(token);
    if (decoded.exp === undefined) {
      return null;
    }

    const date = new Date(0);
    date.setUTCSeconds(decoded.exp);
    return date;
  }
}
