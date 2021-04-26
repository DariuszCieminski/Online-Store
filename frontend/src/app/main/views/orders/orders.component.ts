import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from "@angular/material/paginator";
import { MatSort } from "@angular/material/sort";
import { MatTableDataSource } from "@angular/material/table";
import { interval } from "rxjs";
import { AuthenticationService } from "../../services/authentication/authentication-service";
import { Order } from "../../models/order";
import { OrderStatus } from "../../models/order-status.enum";
import { PaymentMethod } from "../../models/payment-method.enum";
import { OrderService } from "../../services/order.service";

@Component({
    selector: 'app-orders',
    templateUrl: './orders.component.html',
    styleUrls: ['./orders.component.css']
})
export class OrdersComponent implements OnInit {
    isLoading: boolean = true;
    orders: Order[];
    displayedColumns: string[] = ['items', 'deliveryAddress', 'paymentMethod', 'status', 'cost', 'time', 'information'];
    dataSource: MatTableDataSource<Order>;
    @ViewChild(MatPaginator, {static: false}) paginator: MatPaginator;
    @ViewChild(MatSort, {static: false}) sort: MatSort;

    constructor(private orderService: OrderService, private auth: AuthenticationService) {
    }

    ngOnInit(): void {
        //wait for AuthenticationService to initialize
        let loadingSubscriber = interval(50)
            .subscribe(() => {
                if (this.auth.getUser() !== undefined) {
                    this.isLoading = false;
                    loadingSubscriber.unsubscribe();

                    this.orderService.getOrdersByCurrentUser()
                        .subscribe(value => {
                            this.orders = value;
                            this.dataSource = new MatTableDataSource<Order>(this.orders);
                            this.dataSource.paginator = this.paginator;
                            this.dataSource.sort = this.sort;
                        });
                }
            });
    }

    getPaymentMethod(key: string): string {
        return PaymentMethod[key];
    }

    getOrderStatus(key: string): string {
        return OrderStatus[key];
    }
}