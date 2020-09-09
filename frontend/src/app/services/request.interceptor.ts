import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { AuthenticationService } from "./authentication.service";
import { catchError } from "rxjs/operators";
import { Router } from "@angular/router";

@Injectable()
export class RequestInterceptor implements HttpInterceptor {

    constructor(private auth: AuthenticationService, private router: Router) {
    }

    intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        let access = sessionStorage.getItem(this.auth.accessToken);
        let isLogin = request.url.includes('/login');
        let isPostRegister = history.state.register != undefined;

        if (access == null && !isLogin && !isPostRegister) {
            this.router.navigateByUrl('/login');
        } else {
            return next.handle(
                request.clone({
                    withCredentials: true,
                    headers: isLogin ? request.headers : request.headers.append("Authorization", "Bearer " + access)
                })
            ).pipe(
                catchError((err: HttpErrorResponse) => {
                    if (err.status == 401 && err.error === "UNAUTHORIZED") {
                        this.auth.reAuthentication()
                            .subscribe(
                                () => {
                                    access = sessionStorage.getItem(this.auth.accessToken);
                                    return next.handle(
                                        request.clone({
                                            withCredentials: true,
                                            headers: request.headers.append("Authorization", "Bearer " + access)
                                        }));
                                },
                                () => this.router.navigateByUrl('/login'));
                    } else return throwError(err);
                }));
        }
    }
}