import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandler,
    HttpHeaders,
    HttpInterceptor,
    HttpRequest
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from "@angular/router";
import { EMPTY, Observable, throwError } from 'rxjs';
import { catchError, switchMap } from "rxjs/operators";
import { JwtAuthenticationService } from "./jwt-authentication.service";

@Injectable()
export class RequestInterceptor implements HttpInterceptor {

    constructor(private auth: JwtAuthenticationService, private router: Router) {
    }

    intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        let reqHeaders = request.headers;

        if (this.auth.isAuthenticated() || reqHeaders.has("reauth")) {
            reqHeaders = this.appendAuthHeader(reqHeaders).delete("reauth");
        } else if (this.auth.getAccessToken() != null && !request.url.includes("/logout")) {
            return this.auth.reAuthentication().pipe(
                switchMap(() => {
                    return next.handle(request.clone({
                        headers: this.appendAuthHeader(reqHeaders),
                        withCredentials: true
                    }));
                }),
                catchError((error: HttpErrorResponse) => {
                    if (error.status === 401) {
                        this.router.navigateByUrl('/login').then(() => EMPTY);
                    } else {
                        return throwError(error);
                    }
                })
            );
        }
        return next.handle(request.clone({headers: reqHeaders, withCredentials: true}));
    }

    private appendAuthHeader(requestHeaders: HttpHeaders): HttpHeaders {
        return requestHeaders.append("Authorization", "Bearer " + this.auth.getAccessToken());
    }
}