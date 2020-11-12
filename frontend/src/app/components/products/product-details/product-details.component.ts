import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Product } from "../../../models/product";
import { ValidationErrors } from "@angular/forms";
import { CartService } from "../../../services/cart.service";

@Component({
    selector: 'app-product-details',
    templateUrl: './product-details.component.html',
    styleUrls: ['./product-details.component.css']
})
export class ProductDetailsComponent {
    imageIndex: number = 0;
    selectedQuantity: number = 1;
    quantityErrors: ValidationErrors;
    readonly errorMessages: object = {
        'required': 'Product quantity was not specified!',
        'min': 'You have to buy at least one product!',
        'max': 'Product quantity is too big!',
        'empty': 'Product is out of stock!'
    };

    constructor(@Inject(MAT_DIALOG_DATA) public product: Product, private cartService: CartService) {
    }

    changeImage(index: number): void {
        this.imageIndex += index;
        this.imageIndex = Math.min(this.imageIndex, this.product.images.length - 1);
        this.imageIndex = Math.max(this.imageIndex, 0);
    }

    changeQuantity(quantity: number): void {
        this.selectedQuantity = quantity;
        this.quantityErrors = null;
    }

    addProductToCart(): void {
        this.cartService.addProduct(this.product, this.selectedQuantity);
    }
}