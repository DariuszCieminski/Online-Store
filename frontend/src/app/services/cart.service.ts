import { Injectable } from '@angular/core';
import { Product } from "../models/product";
import { OrderItem } from "../models/order-item";
import Big from "big.js";

@Injectable({
    providedIn: 'root'
})
export class CartService {

    constructor() {
    }

    getCartProducts(): OrderItem[] {
        return JSON.parse(sessionStorage.getItem('cart')) || [];
    }

    isCartValid(): boolean {
        for (let item of this.getCartProducts()) {
            if (!(item.quantity > 0 && item.quantity <= item.product.quantity)) {
                return false;
            }
        }
        return true;
    }

    getCartValue(): number {
        return this.getCartProducts()
            .map(item => Big(item.product.price).times(item.quantity))
            .reduce((previousValue, currentValue) => previousValue.plus(currentValue), Big(0))
            .toNumber();
    }

    addProduct(product: Product, quantity: number): void {
        let cartContent = this.getCartProducts();
        cartContent.push(new OrderItem(product, quantity));
        sessionStorage.setItem('cart', JSON.stringify(cartContent));
    }

    modifyProduct(item: OrderItem, newQuantity?: number): void {
        let cartContent = this.getCartProducts();
        let productIndex = cartContent.findIndex(cartItem => cartItem.product.id === item.product.id);

        if (productIndex !== -1) {
            (newQuantity) ? cartContent[productIndex].quantity = newQuantity : cartContent.splice(productIndex, 1);
            sessionStorage.setItem('cart', JSON.stringify(cartContent));
        }
    }
}