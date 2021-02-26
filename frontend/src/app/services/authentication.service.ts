import { HttpClient } from "@angular/common/http";
import { Injectable } from '@angular/core';
import { NgxPermissionsService } from 'ngx-permissions';
import { Observable, of } from "rxjs";
import { catchError, mapTo, tap } from "rxjs/operators";
import { User } from "../models/user";
import { ApiUrls } from "../util/api-urls";

@Injectable({
    providedIn: 'root'
})
export class AuthenticationService {
    public readonly userItem: string = "app_user";
    public readonly accessToken: string = "access_token";
    public readonly refreshToken: string = "refresh_token";
    private currentUser: User;

    constructor(private httpClient: HttpClient, private permissions: NgxPermissionsService) {
        let accessToken = sessionStorage.getItem(this.accessToken);

        if (accessToken && sessionStorage.getItem(this.refreshToken) && sessionStorage.getItem(this.userItem)) {
            this.currentUser = JSON.parse(sessionStorage.getItem(this.userItem));
            this.setUserRolesFromToken(accessToken);
        } else {
            this.clearUserData();
        }
    }

    get getUser(): User | null {
        return this.currentUser;
    }

    isTokenValid(): boolean {
        let token = sessionStorage.getItem(this.accessToken);
        if (token == null) return false;

        let expDate = this.readTokenClaim(token, "exp") * 1000;
        return expDate > new Date().getTime();
    }

    login(loginData: object): Observable<boolean> {
        return this.httpClient.post(ApiUrls.login, loginData)
                   .pipe(
                       tap(value => this.loadUser(value)),
                       mapTo(true),
                       catchError(() => of(false)));
    }

    reAuthentication(): Observable<boolean> {
        let accessToken = sessionStorage.getItem(this.accessToken);
        let refreshToken = sessionStorage.getItem(this.refreshToken);
        let tokens = {"access_token": accessToken, "refresh_token": refreshToken};

        return this.httpClient.post(ApiUrls.login, tokens, {headers: {"reauth": "true"}})
                   .pipe(
                       tap(response => sessionStorage.setItem(this.accessToken, response[this.accessToken])),
                       mapTo(true),
                       catchError(() => {
                           this.clearUserData();
                           return of(false);
                       }));
    }

    logout(): Observable<boolean> {
        return this.httpClient.post(ApiUrls.logout, null)
                   .pipe(
                       tap(() => this.clearUserData()),
                       mapTo(true),
                       catchError(() => of(false)));
    }

    private readTokenClaim(token: string, claim: string): any {
        let tokenClaims = token.split('.')[1];
        return JSON.parse(atob(tokenClaims))[claim];
    }

    private loadUser(response: object): void {
        this.currentUser = response["user"];
        this.setUserRolesFromToken(response[this.accessToken]);
        sessionStorage.setItem(this.userItem, JSON.stringify(this.currentUser));
        sessionStorage.setItem(this.accessToken, response[this.accessToken]);
        sessionStorage.setItem(this.refreshToken, response[this.refreshToken]);
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