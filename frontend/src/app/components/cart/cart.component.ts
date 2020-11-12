import { Component, OnInit } from '@angular/core';
import { CartService } from "../../services/cart.service";
import { OrderItem } from "../../models/order-item";

@Component({
    selector: 'app-cart',
    templateUrl: './cart.component.html',
    styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit {
    cartItems: OrderItem[];
    cartValue: number;
    isCartValid: boolean = true;

    constructor(private cartService: CartService) {
    }

    ngOnInit(): void {
        this.cartItems = this.cartService.getCartProducts();
        this.checkCartValidityAndValue();
    }

    checkCartValidityAndValue(): void {
        this.isCartValid = this.cartService.isCartValid();

        if (this.isCartValid) {
            this.cartValue = this.cartService.getCartValue();
        }
    }

    modifyItemQuantity(item: OrderItem, newQuantity: number): void {
        let modifiedProduct = this.cartItems.find(cartItem => cartItem.product.id === item.product.id);
        modifiedProduct.quantity = newQuantity;
        this.cartService.modifyProduct(item, newQuantity);
        this.checkCartValidityAndValue();
    }

    deleteItem(item: OrderItem): void {
        let index = this.cartItems.findIndex(cartItem => cartItem.product.id === item.product.id);
        this.cartItems.splice(index, 1);
        this.cartService.modifyProduct(item);
        this.checkCartValidityAndValue();
    }
}