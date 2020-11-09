import { Component, OnInit } from '@angular/core';
import { CartService } from "../../services/cart.service";
import { OrderItem } from "../../models/order-item";
import Big from "big.js";

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
        this.calculateCartValue();
    }

    checkCartValidity(): void {
        for (let item of this.cartItems) {
            if (!(item.quantity > 0 && item.quantity <= item.product.quantity)) {
                this.isCartValid = false;
                break;
            }
        }
        this.isCartValid = true;
    }

    calculateCartValue(): void {
        if (this.isCartValid) {
            this.cartValue = this.cartItems
                .map(item => Big(item.product.price).times(item.quantity))
                .reduce((previousValue, currentValue) => previousValue.plus(currentValue), Big(0))
                .toNumber();
        }
    }

    modifyItemQuantity(item: OrderItem, newQuantity: number): void {
        let modifiedProduct = this.cartItems.find(cartItem => cartItem.product.id === item.product.id);
        modifiedProduct.quantity = newQuantity;
        this.cartService.modifyProduct(item, newQuantity);
        this.checkCartValidity();
        this.calculateCartValue();
    }

    deleteItem(item: OrderItem): void {
        let index = this.cartItems.findIndex(cartItem => cartItem.product.id === item.product.id);
        this.cartItems.splice(index, 1);
        this.cartService.modifyProduct(item);
        this.checkCartValidity();
        this.calculateCartValue();
    }
}