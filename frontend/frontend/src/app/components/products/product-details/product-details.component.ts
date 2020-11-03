import { AfterContentInit, Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FormControl, Validators } from "@angular/forms";
import { Product } from "../../../models/product";

@Component({
    selector: 'app-product-details',
    templateUrl: './product-details.component.html',
    styleUrls: ['./product-details.component.css']
})
export class ProductDetailsComponent implements OnInit, AfterContentInit {
    quantity: FormControl;
    imageIndex: number = 0;
    readonly errorMessages: object = {
        'required': 'Product quantity was not specified!',
        'min': 'You have to buy at least one product!',
        'max': 'Product quantity is too big!',
        'empty': 'Product is out of stock!'
    };

    constructor(@Inject(MAT_DIALOG_DATA) public product: Product) {
    }

    ngOnInit(): void {
        this.quantity = new FormControl(1,
            [Validators.required, Validators.min(1), Validators.max(this.product.quantity)]);
    }

    ngAfterContentInit(): void {
        if (!this.product.quantity) {
            this.quantity.disable();
            this.quantity.setErrors({'empty': true});
        }
    }

    changeImage(index: number): void {
        this.imageIndex += index;
        this.imageIndex = Math.min(this.imageIndex, this.product.images.length - 1);
        this.imageIndex = Math.max(this.imageIndex, 0);
    }

    addProductToCart(): void {

    }
}