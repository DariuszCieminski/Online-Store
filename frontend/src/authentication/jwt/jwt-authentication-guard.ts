import { Injectable } from "@angular/core";
import { ActivatedRouteSnapshot, Data, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, mapTo } from "rxjs/operators";
import { AuthenticationGuard } from "../authentication-guard";
import { JwtAuthenticationService } from "./jwt-authentication.service";

@Injectable({
    providedIn: "root"
})
export class JwtAuthenticationGuard extends AuthenticationGuard {

    constructor(private authService: JwtAuthenticationService, private router: Router) {
        super();
    }

    canActivate(
        next: ActivatedRouteSnapshot,
        state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
        if (this.authService.isAuthenticated()) {
            return true;
        } else if (this.authService.getUser()) {
            return this.authService.reAuthentication().pipe(
                mapTo(true),
                catchError(() => of(this.canGuestActivate(next.data)))
            );
        } else {
            return this.canGuestActivate(next.data);
        }
    }

    protected canGuestActivate(routeData: Data): boolean | UrlTree {
        return routeData.guestAllowed ? true : this.router.parseUrl('/login');
    }
}