import { Component, ElementRef, Inject, OnInit, Renderer2, ViewChild } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { Product } from "../../../models/product";
import { Validator } from "../../../util/validator";

@Component({
    selector: 'app-add-product',
    templateUrl: './product-data.component.html',
    styleUrls: ['./product-data.component.css']
})
export class ProductDataComponent implements OnInit {
    productForm: FormGroup;
    @ViewChild('content') dialogContent: ElementRef;

    constructor(@Inject(MAT_DIALOG_DATA) public product: Product, private builder: FormBuilder,
                private dialogRef: MatDialogRef<ProductDataComponent>, private renderer: Renderer2) {
    }

    ngOnInit(): void {
        this.productForm = this.builder.group({
            id: new FormControl(),
            name: new FormControl(null, Validators.required),
            description: new FormControl(),
            price: new FormControl(null, [Validators.required, Validators.min(0.01), Validator.price()]),
            quantity: new FormControl(null, [Validators.required, Validators.min(0)]),
            images: new FormArray([this.createImageField()])
        });

        if (this.product) {
            for (let i = 1; i < this.product.images.length; i++) {
                this.getImageFields().push(this.createImageField());
            }
            this.productForm.patchValue(this.product);
        }
    }

    getImageFields(): FormArray {
        return <FormArray>this.productForm.get("images");
    }

    createImageField(): FormControl {
        return new FormControl('', Validators.pattern("^(http|https)?.*\\/.*"));
    }

    addImageFieldToForm(): void {
        this.getImageFields().push(this.createImageField());
        let formDialog = this.renderer.selectRootElement(this.dialogContent.nativeElement, true);
        setTimeout(() => formDialog.scroll({
            top: formDialog.scrollHeight,
            behavior: 'smooth'
        }));
    }

    removeImageField(index: number): void {
        this.getImageFields().removeAt(index);
    }

    onSubmit(): void {
        if (this.productForm.valid) {
            let value = this.productForm.value;
            value.price = value.price.toString().replace(',', '.');
            value.images = value.images.filter(i => i != '');

            this.dialogRef.close(value);
        }
    }
}