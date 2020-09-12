import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Data, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthenticationService } from "./authentication.service";
import { tap } from "rxjs/operators";

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
                tap(success => {
                    if (success) return true;
                    return this.allowOrRedirect(next.data);
                }));
        }
    }

    private allowOrRedirect(data: Data): boolean | Promise<boolean> {
        return data.canAnonymous ? true : this.router.navigateByUrl('/');
    }
}