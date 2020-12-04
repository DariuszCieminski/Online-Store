import { HttpEvent, HttpHandler, HttpHeaders, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from "@angular/router";
import { Observable } from 'rxjs';
import { switchMap } from "rxjs/operators";
import { AuthenticationService } from "../services/authentication.service";

@Injectable()
export class RequestInterceptor implements HttpInterceptor {

    constructor(private auth: AuthenticationService, private router: Router) {
    }

    intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        let token = sessionStorage.getItem(this.auth.accessToken);
        let isReauth = request.headers.has("reauth");
        let isLogin = request.url.includes('/login') && !isReauth;
        let isRegister = request.url.includes('/api/users') && request.method == 'POST';
        let reqHeaders = request.headers;

        if (this.auth.isTokenValid() || isReauth) {
            reqHeaders = this.appendAuthHeader(reqHeaders, token).delete('reauth');
        } else if (!isLogin && !isRegister) {
            return this.auth.reAuthentication().pipe(
                switchMap(success => {
                    if (success) {
                        token = sessionStorage.getItem(this.auth.accessToken)
                        return next.handle(
                            request.clone({
                                withCredentials: true,
                                headers: this.appendAuthHeader(reqHeaders, token)
                            }));
                    } else this.router.navigateByUrl('/login');
                })
            );
        }
        return next.handle(request.clone({headers: reqHeaders, withCredentials: true}));
    }

    private appendAuthHeader(headers: HttpHeaders, authToken: string): HttpHeaders {
        return headers.append("Authorization", "Bearer " + authToken);
    }
}