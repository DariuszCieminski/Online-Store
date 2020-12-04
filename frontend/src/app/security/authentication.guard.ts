import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Data, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from "rxjs/operators";
import { AuthenticationService } from "../services/authentication.service";

@Injectable({
    providedIn: 'root'
})
export class AuthenticationGuard implements CanActivate {

    constructor(private auth: AuthenticationService, private router: Router) {
    }

    canActivate(
        next: ActivatedRouteSnapshot,
        state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
        if (this.auth.isTokenValid()) {
            return true;
        } else {
            if (!this.auth.getUser) return this.allowOrRedirect(next.data);
            return this.auth.reAuthentication().pipe(
                map(success => success ? true : this.allowOrRedirect(next.data)));
        }
    }

    private allowOrRedirect(data: Data): boolean | UrlTree {
        return data.canAnonymous ? true : this.router.parseUrl('/');
    }
}