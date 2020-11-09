import { Injectable } from '@angular/core';
import { Product } from "../models/product";
import { OrderItem } from "../models/order-item";

@Injectable({
    providedIn: 'root'
})
export class CartService {

    constructor() {
    }

    getCartProducts(): OrderItem[] {
        return JSON.parse(sessionStorage.getItem('cart')) || [];
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