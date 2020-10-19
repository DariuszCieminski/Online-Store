import { Component } from '@angular/core';
import { AuthenticationService } from "../../services/authentication.service";

@Component({
    selector: 'navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {

    constructor(private auth: AuthenticationService) {
    }

    getUser(): string {
        return this.auth.getUser;
    }

    doLogout(): void {
        this.auth.logout().subscribe(() => window.location.reload());
    }
}