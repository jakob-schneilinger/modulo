import { Injectable } from "@angular/core";
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from "@angular/common/http";
import { AuthService } from "../services/auth.service";
import { Observable } from "rxjs";
import { Globals } from "../global/globals";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  ignoreUrls: string[];

  constructor(private authService: AuthService, private globals: Globals) {
    this.ignoreUrls = [
      this.globals.backendUri + "/authentication",
      this.globals.backendUri + "/user/register",
    ];
  }

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    // Do not intercept authentication requests
    if (this.ignoreUrls.includes(req.url)) {
      return next.handle(req);
    }

    const authReq = req.clone({
      headers: req.headers.set("Authorization", this.authService.getToken()),
    });

    return next.handle(authReq);
  }
}
