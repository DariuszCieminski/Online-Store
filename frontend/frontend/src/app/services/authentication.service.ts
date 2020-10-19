import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { NgxPermissionsService } from 'ngx-permissions';
import { User } from "../models/user";
import { Observable, of } from "rxjs";
import { catchError, mapTo, tap } from "rxjs/operators";

@Injectable({
    providedIn: 'root'
})
export class AuthenticationService {
    public readonly userItem: string = "app_user";
    public readonly accessToken: string = "access_token";
    public readonly refreshToken: string = "refresh_token";
    private user: User = new User();

    constructor(private httpClient: HttpClient, private permissions: NgxPermissionsService) {
        let loggedUser: string = sessionStorage.getItem(this.userItem);

        if (loggedUser && sessionStorage.getItem(this.accessToken) && sessionStorage.getItem(this.refreshToken)) {
            this.user = JSON.parse(loggedUser);
            this.permissions.loadPermissions(this.user.roles);
        } else {
            this.clearUserData();
        }
    }

    get getUser(): string {
        return this.user ? (this.user.firstName + ' ' + this.user.lastName) : null;
    }

    isTokenValid(): boolean {
        let token = sessionStorage.getItem(this.accessToken);
        if (token == null) return false;

        let tokenClaims = token.split('.')[1];
        let expDate = JSON.parse(atob(tokenClaims))["exp"] * 1000;
        return expDate > new Date().getTime();
    }

    login(loginData: object): Observable<boolean> {
        return this.httpClient.post('http://localhost:8080/login', loginData)
            .pipe(
                tap(value => this.loadUser(value)),
                mapTo(true),
                catchError(() => of(false)));
    }

    loadUser(response: object): void {
        this.user = response["user"];
        this.user.roles = this.user.roles.map(role => role.substring(5));
        this.permissions.loadPermissions(this.user.roles);
        sessionStorage.setItem(this.userItem, JSON.stringify(this.user));
        sessionStorage.setItem(this.accessToken, response[this.accessToken]);
        sessionStorage.setItem(this.refreshToken, response[this.refreshToken]);
    }

    reAuthentication(): Observable<boolean> {
        let accessToken = sessionStorage.getItem(this.accessToken);
        let refreshToken = sessionStorage.getItem(this.refreshToken);
        let tokens = {"access_token": accessToken, "refresh_token": refreshToken};

        return this.httpClient.post('http://localhost:8080/login', tokens, {headers: {"reauth": "true"}})
            .pipe(
                tap(response => sessionStorage.setItem(this.accessToken, response[this.accessToken])),
                mapTo(true),
                catchError(() => {
                    this.clearUserData();
                    return of(false);
                }));
    }

    logout(): Observable<boolean> {
        return this.httpClient.post('http://localhost:8080/logout', null)
            .pipe(
                tap(() => this.clearUserData()),
                mapTo(true),
                catchError(() => of(false)));
    }

    private clearUserData(): void {
        sessionStorage.clear();
        this.user = null;
        this.permissions.flushPermissions();
        this.permissions.addPermission('GUEST');
    }
}