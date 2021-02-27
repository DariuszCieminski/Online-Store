import { NoopScrollStrategy } from "@angular/cdk/overlay";
import { HttpErrorResponse } from "@angular/common/http";
import { Component, ElementRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { Product } from "../../models/product";
import { ProductService } from "../../services/product.service";
import { SnackbarService } from "../../services/snackbar.service";
import { ProductDataComponent } from "./product-data/product-data.component";
import { ProductDeleteComponent } from "./product-delete.component";
import { ProductDetailsComponent } from './product-details/product-details.component';

@Component({
    selector: 'app-products',
    templateUrl: './products.component.html',
    styleUrls: ['./products.component.css']
})
export class ProductsComponent {
    allProducts: Product[] = [];
    paginated: Product[] = [];
    isLoading: boolean = false;
    @ViewChild(MatPaginator, {static: false}) paginator: MatPaginator;
    @ViewChild('search_box', {static: false}) searchBox: ElementRef;

    constructor(private productService: ProductService, private dialog: MatDialog, private snackBar: SnackbarService) {
    }

    onPageChange(event: PageEvent): void {
        let start: number = event.pageIndex * event.pageSize;
        let end: number = start + event.pageSize;
        this.paginated = this.allProducts.slice(start, end);
    }

    searchProducts(filters?: object): void {
        if (!filters) filters = {};
        let productName = this.searchBox.nativeElement.value;
        if (productName.toString().trim() !== '') {
            filters["nameContains"] = productName;
        }
        this.isLoading = true;

        this.productService.getProducts(filters)
            .subscribe(response => {
                this.allProducts = response;
                this.paginated = this.allProducts;
                this.paginator.length = this.paginated.length;
                this.isLoading = false;
            }, error => this.handleError(error));
    }

    showProductDetails(product: Product): void {
        this.dialog.open(ProductDetailsComponent, {
            height: 'auto',
            width: '60%',
            data: product,
            scrollStrategy: new NoopScrollStrategy()
        }).afterClosed()
            .subscribe(isAddedToCart => {
                if (isAddedToCart === true) {
                    this.showSnackBar(`Product '${product.name}' was added to the cart.`);
                } else if (isAddedToCart === false) {
                    this.showSnackBar(`Product '${product.name}' is already in the cart!`);
                }
            });
    }

    showProductDataDialog(p?: Product): void {
        this.dialog.open(ProductDataComponent, {
            height: 'auto',
            width: '40%',
            data: p,
            disableClose: true,
            scrollStrategy: new NoopScrollStrategy()
        }).afterClosed()
            .subscribe(value => {
                if (value !== '') {
                    if (value.id) {
                        return this.productService.updateProduct(value)
                            .subscribe(() => this.showSnackBar(`Product '${p.name}' was successfully modified.`),
                                error => this.handleError(error));
                    } else {
                        return this.productService.addProduct(value)
                            .subscribe(product => this.showSnackBar(`Product '${product.name}' was successfully added.`),
                                error => this.handleError(error));
                    }
                }
            });
    }

    showProductDeleteDialog(id: number): void {
        this.dialog.open(ProductDeleteComponent, {
            height: 'auto',
            width: '30%',
            scrollStrategy: new NoopScrollStrategy()
        }).afterClosed()
            .subscribe(value => {
                if (value) {
                    return this.productService.deleteProduct(id)
                        .subscribe(() => {
                            let index = this.paginated.findIndex(product => product.id === id);
                            this.paginated.splice(index);
                            this.showSnackBar('Product was deleted successfully');
                        }, error => this.handleError(error));
                }
            });
    }

    private showSnackBar(message: string): void {
        this.snackBar.showSnackbar(message, "OK", {duration: 0});
    }

    private handleError(error: HttpErrorResponse): void {
        this.showSnackBar(`${error.name} (status ${error.status}): ${error.message}`);
    }
}