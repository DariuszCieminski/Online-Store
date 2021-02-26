import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { Order } from "../../models/order";
import { OrderItem } from "../../models/order-item";
import { PaymentMethod } from "../../models/payment-method.enum";
import { User } from "../../models/user";
import { AuthenticationService } from "../../services/authentication.service";
import { CartService } from "../../services/cart.service";
import { OrderService } from "../../services/order.service";
import { SnackbarService } from "../../services/snackbar.service";
import { Validator } from "../../util/validator";

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

    constructor(private builder: FormBuilder, private router: Router, private cartService: CartService,
                private orderService: OrderService, private auth: AuthenticationService, private snackBar: SnackbarService) {
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
            paymentMethod: new FormControl(
                this.paymentMethods[Object.values(PaymentMethod).indexOf(PaymentMethod.BANK_TRANSFER)], Validators.required),
            information: new FormControl(null),
            deliveryAddress: new FormGroup({
                street: new FormControl(this.user.address ? this.user.address.street : null, Validators.required),
                postCode: new FormControl(this.user.address ? this.user.address.postCode : null,
                           [Validators.required, Validator.postCode()]),
                city: new FormControl(this.user.address ? this.user.address.city : null, Validators.required)
            })
        });
    }

    getPaymentName(key: string): string {
        return PaymentMethod[key];
    }

    createOrder(): void {
        let order = new Order(this.cartContent, this.shippingData.get('deliveryAddress').value,
                              this.shippingData.get('paymentMethod').value, this.shippingData.get('information').value);

        this.orderService.createOrder(order)
            .subscribe(() => {
                this.cartService.clearCart();
                this.router.navigateByUrl('/')
                    .then(() => this.snackBar.showSnackbar('Order has been made successfully.', 'OK', {duration: 0}));
            });
    }
}