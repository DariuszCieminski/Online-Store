import { Injectable } from "@angular/core";
import { ActivatedRouteSnapshot, Data, Router, RouterStateSnapshot, UrlTree } from "@angular/router";
import { Observable } from "rxjs";
import { map } from "rxjs/operators";
import { AuthenticationGuard } from "../authentication-guard";
import { SessionAuthenticationService } from "./session-authentication.service";

@Injectable({
    providedIn: "root"
})
export class SessionAuthenticationGuard extends AuthenticationGuard {
    constructor(private authService: SessionAuthenticationService, private router: Router) {
        super();
    }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
        return this.authService.isAuthenticated().pipe(
            map(authenticated => authenticated ? true : this.canGuestActivate(route.data))
        );
    }

    protected canGuestActivate(routeData: Data): boolean | UrlTree {
        return routeData.guestAllowed ? true : this.router.parseUrl("/login");
    }
}