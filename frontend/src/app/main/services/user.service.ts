import { HttpClient } from "@angular/common/http";
import { Injectable } from '@angular/core';
import { Observable } from "rxjs";
import { UserDetailed } from "../../admin/models/user-detailed";
import { ApiUrls } from "../util/api-urls";

@Injectable({
    providedIn: 'root'
})
export class UserService {

    constructor(private httpClient: HttpClient) {
    }

    updateUser(user: UserDetailed): Observable<UserDetailed> {
        return this.httpClient.put<UserDetailed>(ApiUrls.users, user);
    }

    deleteUser(id: number): Observable<void> {
        return this.httpClient.delete<void>(ApiUrls.users + `/${id}`);
    }
}