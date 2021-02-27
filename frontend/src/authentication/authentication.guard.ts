import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Data, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, mapTo } from "rxjs/operators";
import { AuthenticationService } from "./authentication.service";

@Injectable({
    providedIn: 'root'
})
export class AuthenticationGuard implements CanActivate {

    constructor(private auth: AuthenticationService, private router: Router) {
    }

    canActivate(
        next: ActivatedRouteSnapshot,
        state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
        if (this.auth.isAuthenticated()) {
            return true;
        } else if (this.auth.getUser()) {
            return this.auth.reAuthentication().pipe(
                mapTo(true),
                catchError(() => of(this.allowOrRedirect(next.data)))
            );
        } else {
            return this.allowOrRedirect(next.data);
        }
    }

    private allowOrRedirect(data: Data): boolean | UrlTree {
        return data.canAnonymous ? true : this.router.parseUrl('/login');
    }
}