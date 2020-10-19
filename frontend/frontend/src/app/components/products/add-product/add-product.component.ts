import { Component, ElementRef, OnInit, Renderer2, ViewChild } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { MatDialogRef } from "@angular/material/dialog";
import { Validator } from "../../../util/validator";

@Component({
    selector: 'app-add-product',
    templateUrl: './add-product.component.html',
    styleUrls: ['./add-product.component.css']
})
export class AddProductComponent implements OnInit {
    productForm: FormGroup;
    @ViewChild('content') dialogContent: ElementRef;

    constructor(private builder: FormBuilder, private dialogRef: MatDialogRef<AddProductComponent>, private renderer: Renderer2) {
    }

    ngOnInit(): void {
        this.productForm = this.builder.group({
            name: new FormControl(null, Validators.required),
            description: new FormControl(),
            price: new FormControl(null, [Validators.required, Validators.min(0.01), Validator.PriceValidator()]),
            quantity: new FormControl(null, [Validators.required, Validators.min(0)]),
            images: new FormArray([this.createImageField()])
        });
    }

    getImageFields(): FormArray {
        return <FormArray>this.productForm.get("images");
    }

    createImageField(): FormControl {
        return new FormControl(null, Validators.pattern("^(http|https)?.*\\/.*"));
    }

    addImageFieldToForm(): void {
        this.getImageFields().push(this.createImageField());
        let formDialog = this.renderer.selectRootElement(this.dialogContent.nativeElement, true);
        setTimeout(() => formDialog.scroll({
            top: formDialog.scrollHeight,
            behavior: 'smooth'
        }), 1);
    }

    removeImageField(index: number): void {
        this.getImageFields().removeAt(index);
    }

    onSubmit(): void {
        if (this.productForm.valid) {
            let value = this.productForm.value;
            value.price = value.price.replace(',', '.');
            value.images = value.images.filter(i => i != null);

            this.dialogRef.close(value);
        }
    }
}