import { Product } from "./product";

export class OrderItem {
    id: number;
    product: Product;
    quantity: number;

    constructor(product: Product, quantity: number) {
        this.product = product;
        this.quantity = quantity;
    }
}