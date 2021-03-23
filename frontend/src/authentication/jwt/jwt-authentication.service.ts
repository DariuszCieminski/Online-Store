import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { NgxPermissionsService } from "ngx-permissions";
import { Observable, of } from "rxjs";
import { catchError, mapTo, tap } from "rxjs/operators";
import { ApiUrls } from "../../app/util/api-urls";
import { AuthenticationService } from "../authentication-service";

@Injectable({
    providedIn: "root"
})
export class JwtAuthenticationService extends AuthenticationService {
    private readonly USER_KEY: string = "app_user";
    private readonly ACCESS_TOKEN: string = "access_token";
    private readonly REFRESH_TOKEN: string = "refresh_token";

    constructor(private httpClient: HttpClient, private permissions: NgxPermissionsService) {
        super();
        let accessToken = sessionStorage.getItem(this.ACCESS_TOKEN);

        if (accessToken && sessionStorage.getItem(this.REFRESH_TOKEN) && sessionStorage.getItem(this.USER_KEY)) {
            this.currentUser = JSON.parse(sessionStorage.getItem(this.USER_KEY));
            this.setUserRolesFromToken(accessToken);
        } else {
            this.clearUserData();
        }
    }

    isAuthenticated(): boolean {
        let token = sessionStorage.getItem(this.ACCESS_TOKEN);
        if (!token) return false;

        let tokenExpTime = this.readTokenClaim(token, "exp") * 1000;
        return tokenExpTime > new Date().getTime();
    }

    login(loginData: object): Observable<boolean> {
        return this.httpClient.post(ApiUrls.login, loginData)
                   .pipe(
                       tap(value => this.loadUser(value)),
                       mapTo(true),
                       catchError(() => of(false)));
    }

    logout(): Observable<boolean> {
        return this.httpClient.post(ApiUrls.logout, null)
                   .pipe(
                       tap(() => this.clearUserData()),
                       mapTo(true),
                       catchError(() => of(false)));
    }

    reAuthentication(): Observable<Object> {
        let accessToken = sessionStorage.getItem(this.ACCESS_TOKEN);
        let refreshToken = sessionStorage.getItem(this.REFRESH_TOKEN);
        let tokens = {"access_token": accessToken, "refresh_token": refreshToken};

        return this.httpClient.post(ApiUrls.login, tokens, {headers: {"reauth": "yes"}}).pipe(
            tap(response => sessionStorage.setItem(this.ACCESS_TOKEN, response[this.ACCESS_TOKEN]),
                () => this.clearUserData())
        );
    }

    getAccessToken(): string {
        return sessionStorage.getItem(this.ACCESS_TOKEN);
    }

    private readTokenClaim(token: string, claim: string): any {
        let tokenClaims = token.split('.')[1];
        return JSON.parse(atob(tokenClaims))[claim];
    }

    private loadUser(response: object): void {
        this.currentUser = response["user"];
        this.setUserRolesFromToken(response[this.ACCESS_TOKEN]);
        sessionStorage.setItem(this.USER_KEY, JSON.stringify(this.currentUser));
        sessionStorage.setItem(this.ACCESS_TOKEN, response[this.ACCESS_TOKEN]);
        sessionStorage.setItem(this.REFRESH_TOKEN, response[this.REFRESH_TOKEN]);
    }

    private setUserRolesFromToken(token: string): void {
        let roles: string[] = this.readTokenClaim(token, "roles")
                                  .map((role: string) => role.startsWith('ROLE_') ? role.substring(5) : role);
        this.permissions.loadPermissions(roles);
    }

    private clearUserData(): void {
        sessionStorage.clear();
        this.currentUser = null;
        this.permissions.flushPermissions();
        this.permissions.addPermission('GUEST');
    }
}