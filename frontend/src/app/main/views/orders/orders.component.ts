import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from "@angular/material/paginator";
import { MatSort } from "@angular/material/sort";
import { MatTableDataSource } from "@angular/material/table";
import { interval } from "rxjs";
import { AuthenticationService } from "../../services/authentication/authentication-service";
import { Order } from "../../models/order";
import { OrderService } from "../../services/order.service";

@Component({
    selector: 'app-orders',
    templateUrl: './orders.component.html',
    styleUrls: ['./orders.component.css']
})
export class OrdersComponent implements OnInit {
    isLoading: boolean = true;
    dataSource: MatTableDataSource<Order>;
    displayedColumns: string[] = ['items', 'deliveryAddress', 'paymentMethod', 'status', 'cost', 'time', 'information'];
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
                        .subscribe(response => {
                            this.dataSource = new MatTableDataSource<Order>(response);
                            this.dataSource.paginator = this.paginator;
                            this.dataSource.sort = this.sort;
                        });
                }
            });
    }
}