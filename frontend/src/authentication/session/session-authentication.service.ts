import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { NgxPermissionsService } from "ngx-permissions";
import { Observable, of } from "rxjs";
import { catchError, mapTo, tap } from "rxjs/operators";
import { User } from "../../app/models/user";
import { ApiUrls } from "../../app/util/api-urls";
import { AuthenticationService } from "../authentication-service";

@Injectable({
    providedIn: "root"
})
export class SessionAuthenticationService extends AuthenticationService {

    constructor(private httpClient: HttpClient, private permissions: NgxPermissionsService) {
        super();
        this.httpClient.get(ApiUrls.ping).subscribe(
            response => this.loadUser(response),
            () => this.clearUserData()
        );
    }

    isAuthenticated(): Observable<boolean> {
        return this.httpClient.get(ApiUrls.ping).pipe(
            mapTo(true),
            catchError(() => {
                this.clearUserData();
                return of(false)
            }));
    }

    login(loginData: object): Observable<boolean> {
        return this.httpClient.post(ApiUrls.login, loginData).pipe(
            tap(response => this.loadUser(response)),
            mapTo(true),
            catchError(() => of(false)));
    }

    logout(): Observable<boolean> {
        return this.httpClient.post(ApiUrls.logout, null).pipe(
            tap(() => this.clearUserData()),
            mapTo(true),
            catchError(() => of(false)));
    }

    private loadUser(responseBody: object): void {
        this.currentUser = <User>responseBody;
        this.permissions.loadPermissions(responseBody["roles"]);
    }

    private clearUserData(): void {
        this.currentUser = null;
        this.permissions.flushPermissions();
        this.permissions.addPermission('GUEST');
    }
}