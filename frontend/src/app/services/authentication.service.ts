import { HttpClient } from "@angular/common/http";
import { Injectable } from '@angular/core';
import { NgxPermissionsService } from 'ngx-permissions';
import { Observable, of } from "rxjs";
import { catchError, mapTo, tap } from "rxjs/operators";
import { User } from "../models/user";
import { ApiUrls } from "../util/api-urls";
import { UserService } from "./user.service";

@Injectable({
    providedIn: 'root'
})
export class AuthenticationService {
    public readonly userItem: string = "app_user";
    public readonly accessToken: string = "access_token";
    public readonly refreshToken: string = "refresh_token";
    private user: User;

    constructor(private httpClient: HttpClient, private permissions: NgxPermissionsService, private userService: UserService) {
        let accessToken = sessionStorage.getItem(this.accessToken);

        if (accessToken && sessionStorage.getItem(this.refreshToken) && sessionStorage.getItem(this.userItem)) {
            let userId = this.readTokenClaim(accessToken, "userId");
            if (userId) {
                setTimeout(() =>
                    this.userService.getCurrentUser()
                        .subscribe(value => {
                            this.user = value;
                            this.user.id = userId;
                            this.setRolesFromToken(accessToken);
                        }, () => this.clearUserData()));
            } else {
                this.user = JSON.parse(sessionStorage.getItem(this.userItem));
                this.setRolesFromToken(accessToken);
            }
        } else {
            this.clearUserData();
        }
    }

    get getUser(): User {
        return this.user;
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
        this.user = response["user"];
        this.user.id = this.readTokenClaim(response[this.accessToken], 'userId');
        this.setRolesFromToken(response[this.accessToken]);
        sessionStorage.setItem(this.userItem, JSON.stringify(this.user));
        sessionStorage.setItem(this.accessToken, response[this.accessToken]);
        sessionStorage.setItem(this.refreshToken, response[this.refreshToken]);
    }

    private setRolesFromToken(accessToken: string): void {
        this.user.roles = this.readTokenClaim(accessToken, "roles")
                              .map((role: string) => role.startsWith('ROLE_') ? role.substring(5) : role);
        this.permissions.loadPermissions(this.user.roles);
    }

    private clearUserData(): void {
        sessionStorage.clear();
        this.user = null;
        this.permissions.flushPermissions();
        this.permissions.addPermission('GUEST');
    }
}