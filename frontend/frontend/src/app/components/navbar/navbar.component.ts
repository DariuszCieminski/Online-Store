import { Component } from '@angular/core';
import { AuthenticationService } from "../../services/authentication.service";
import { Router } from "@angular/router";

@Component({
    selector: 'navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {

    constructor(private auth: AuthenticationService, private router: Router) {
    }

    getUser(): string {
        return this.auth.getUser;
    }

    doLogout(): void {
        this.auth.logout().subscribe(() => this.router.navigateByUrl("/"));
    }
}