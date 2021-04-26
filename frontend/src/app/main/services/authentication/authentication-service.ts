import { Observable } from "rxjs";
import { User } from "../../models/user";

export abstract class AuthenticationService {
    protected currentUser: User;

    getUser(): User | null {
        return this.currentUser;
    }

    abstract isAuthenticated(): boolean | Observable<boolean>;

    abstract login(loginData: object): Observable<boolean>;

    abstract logout(): Observable<boolean>;
}