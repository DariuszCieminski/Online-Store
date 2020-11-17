import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { Order } from "../models/order";

@Injectable({
    providedIn: 'root'
})
export class OrderService {

    constructor(private http: HttpClient) {
    }

    getAllOrders(): Observable<Order[]> {
        return this.http.get<Order[]>('http://localhost:8080/api/orders');
    }

    getOrdersByUserId(id: number): Observable<Order[]> {
        return this.http.get<Order[]>(`http://localhost:8080/api/orders/buyer/${id}`);
    }

    createOrder(order: Order): Observable<Order> {
        return this.http.post<Order>('http://localhost:8080/api/orders', order);
    }

    deleteOrder(id: number): Observable<void> {
        return this.http.delete<void>(`http://localhost:8080/api/orders/${id}`);
    }
}