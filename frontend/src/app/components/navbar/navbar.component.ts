import { Component } from '@angular/core';
import { Router } from "@angular/router";
import { AuthenticationService } from "../../../authentication/authentication-service";
import { CartService } from "../../services/cart.service";
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
        let user = this.auth.getUser();
        if (user) {
            return user.name + ' ' + user.surname;
        }
        return "";
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