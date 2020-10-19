import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthenticationService } from "../services/authentication.service";
import { Router } from "@angular/router";
import { switchMap } from "rxjs/operators";

@Injectable()
export class RequestInterceptor implements HttpInterceptor {

    constructor(private auth: AuthenticationService, private router: Router) {
    }

    intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        let token = sessionStorage.getItem(this.auth.accessToken);
        let isReauth = request.headers.has("reauth");
        let isLogin = request.url.includes('/login') && !isReauth;
        let isPostRegister = history.state.register != undefined;

        if (token == null && !isLogin && !isPostRegister) {
            this.router.navigateByUrl('/login');
        } else {
            if (isLogin) {
                return next.handle(request.clone({withCredentials: true}));
            } else if (isReauth || this.auth.isTokenValid()) {
                return next.handle(
                    request.clone({
                        withCredentials: true,
                        headers: request.headers.append("Authorization", "Bearer " + token).delete("reauth")
                    }));
            } else {
                return this.auth.reAuthentication().pipe(
                    switchMap(success => {
                        if (success) {
                            token = sessionStorage.getItem(this.auth.accessToken)
                            return next.handle(
                                request.clone({
                                    withCredentials: true,
                                    headers: request.headers.append("Authorization", "Bearer " + token)
                                }));
                        } else this.router.navigateByUrl('/login');
                    })
                );
            }
        }
    }
}