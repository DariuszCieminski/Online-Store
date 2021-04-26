import { ActivatedRouteSnapshot, CanActivate, Data, RouterStateSnapshot, UrlTree } from "@angular/router";
import { Observable } from "rxjs";

export abstract class AuthenticationGuard implements CanActivate {
    abstract canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree;

    protected abstract canGuestActivate(routeData: Data): boolean | UrlTree;
}