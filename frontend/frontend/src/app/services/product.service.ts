import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from "@angular/common/http";
import { Product } from "../models/product";
import { Observable } from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class ProductService {
    private readonly predicates: string[] = ["nameContains", "descContains", "priceGreaterThan", "priceLessThan", "priceEqualTo"];

    constructor(private http: HttpClient) {
    }

    getProducts(filters: object): Observable<Product[]> {
        let httpParams = new HttpParams();
        for (let predicate of this.predicates) {
            if (filters.hasOwnProperty(predicate)) httpParams = httpParams.append(predicate, filters[predicate]);
        }

        return this.http.get<Product[]>('http://localhost:8080/api/products', {params: httpParams});
    }

    addProduct(product: Product): Observable<Product> {
        return this.http.post<Product>('http://localhost:8080/api/products', product);
    }

    updateProduct(product: Product): Observable<Product> {
        return this.http.put<Product>('http://localhost:8080/api/products', product);
    }

    deleteProduct(id: number): Observable<void> {
        return this.http.delete<void>(`http://localhost:8080/api/products/${id}`);
    }
}