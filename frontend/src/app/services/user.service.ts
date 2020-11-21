import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { User } from "../models/user";
import { ApiUrls } from "../util/api-urls";

@Injectable({
    providedIn: 'root'
})
export class UserService {

    constructor(private httpClient: HttpClient) {
    }

    getCurrentUser(): Observable<User> {
        return this.httpClient.get<User>(ApiUrls.currentUser);
    }
}