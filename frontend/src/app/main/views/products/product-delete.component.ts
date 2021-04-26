import { Component } from '@angular/core';

@Component({
    selector: 'app-product-delete',
    template: `
        <div class="text">
            Are you sure you want to delete this product?
        </div>
        <div class="actions" fxLayout="row" fxLayoutAlign="space-evenly center">
            <button mat-flat-button color="primary" class="button" matDialogClose="true">
                <mat-icon matPrefix>
                    delete
                </mat-icon>
                Delete
            </button>
            <button mat-flat-button color="warn" class="button" matDialogClose>
                Cancel
            </button>
        </div>`,
    styles: [`
      .text {
        text-align: center;
        font-size: large;
        padding: 1rem 0;
      }

      .actions {
        padding: 1rem 0;
        border-top: 1px solid black;
      }

      .button {
        font-size: larger;
      }`]
})
export class ProductDeleteComponent {
}