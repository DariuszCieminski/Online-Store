import { Observable } from "rxjs";
import { User } from "../app/models/user";

export interface AuthService {
    getUser(): User | null;

    isAuthenticated(): boolean;

    login(loginData: object): Observable<boolean>;

    logout(): Observable<boolean>;
}