import { HttpClient } from "@angular/common/http";
import { Injectable } from '@angular/core';
import { Observable } from "rxjs";
import { Order } from "../models/order";
import { ApiUrls } from "../util/api-urls";

@Injectable({
    providedIn: 'root'
})
export class OrderService {

    constructor(private http: HttpClient) {
    }

    getAllOrders(): Observable<Order[]> {
        return this.http.get<Order[]>(ApiUrls.orders);
    }

    getOrdersByUserId(id: number): Observable<Order[]> {
        return this.http.get<Order[]>(ApiUrls.ordersForBuyer(id));
    }

    getOrdersByCurrentUser(): Observable<Order[]> {
        return this.http.get<Order[]>(ApiUrls.ordersForCurrentUser());
    }

    createOrder(order: Order): Observable<Order> {
        return this.http.post<Order>(ApiUrls.orders, order);
    }

    deleteOrder(id: number): Observable<void> {
        return this.http.delete<void>(ApiUrls.orders + `/${id}`);
    }
}