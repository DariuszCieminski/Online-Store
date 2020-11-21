import { Component } from '@angular/core';
import { AuthenticationService } from "../../services/authentication.service";
import { CartService } from "../../services/cart.service";
import { Router } from "@angular/router";
import { ApiUrls } from "../../util/api-urls";

@Component({
    selector: 'navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {

    constructor(private auth: AuthenticationService, private router: Router, private cart: CartService) {
    }

    getUser(): string {
        return this.auth.getUser ? this.auth.getUser.name + ' ' + this.auth.getUser.surname : "";
    }

    getSwaggerUrl(): string {
        return ApiUrls.swagger;
    }

    getCartItemCount(): number {
        return this.cart.getCartProducts().length;
    }

    doLogout(): void {
        this.auth.logout().subscribe(() => this.router.navigateByUrl("/"));
    }
}