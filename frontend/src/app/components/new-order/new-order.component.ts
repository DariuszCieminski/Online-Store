import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { CartService } from "../../services/cart.service";
import { AuthenticationService } from "../../services/authentication.service";
import { User } from "../../models/user";
import { PaymentMethod } from "../../models/payment-method.enum";
import { OrderItem } from "../../models/order-item";

@Component({
    selector: 'app-new-order',
    templateUrl: './new-order.component.html',
    styleUrls: ['./new-order.component.css']
})
export class NewOrderComponent implements OnInit {
    shippingData: FormGroup;
    cartValue: number;
    cartContent: OrderItem[];
    paymentMethods: string[];
    user: User;

    constructor(private builder: FormBuilder, private router: Router, private cartService: CartService, private auth: AuthenticationService) {
    }

    ngOnInit(): void {
        if (!history.state.createOrder) {
            this.router.navigateByUrl('/');
        }

        this.user = this.auth.getUser;
        this.paymentMethods = Object.keys(PaymentMethod);
        this.cartValue = this.cartService.getCartValue();
        this.cartContent = this.cartService.getCartProducts();

        this.shippingData = this.builder.group({
            paymentMethod: new FormControl(this.paymentMethods[Object.values(PaymentMethod).indexOf(PaymentMethod.BANK_TRANSFER)], Validators.required),
            information: new FormControl(null),
            address: new FormGroup({
                street: new FormControl(this.user.address ? this.user.address.street : null, Validators.required),
                postCode: new FormControl(this.user.address ? this.user.address.postCode : null, [Validators.required, Validators.pattern("^[0-9]{2}-[0-9]{3}$")]),
                city: new FormControl(this.user.address ? this.user.address.city : null, Validators.required)
            })
        });
    }

    getPaymentName(key: string): string {
        return PaymentMethod[key];
    }
}