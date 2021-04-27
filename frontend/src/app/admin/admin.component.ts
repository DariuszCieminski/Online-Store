import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { User } from "../main/models/user";
import { Order } from "../main/models/order";
import { ApiUrls } from "../main/util/api-urls";
import { HttpClient } from "@angular/common/http";
import { MatTabGroup } from "@angular/material/tabs";
import { MatTableDataSource } from "@angular/material/table";

@Component({
    selector: 'app-admin',
    templateUrl: './admin.component.html',
    styleUrls: ['./admin.component.css']
})
export class AdminComponent implements AfterViewInit {
    @ViewChild(MatTabGroup) tabs: MatTabGroup;
    usersDataSource: MatTableDataSource<User>;
    ordersDataSource: MatTableDataSource<Order>;
    userDisplayedCols: string[] = ['name', 'surname', 'email', 'address'];
    orderDisplayedCols: string[] = ['id', 'buyer', 'items', 'deliveryAddress', 'paymentMethod',
                                    'status', 'cost', 'time', 'information'];

    constructor(private httpClient: HttpClient) {
    }

    ngAfterViewInit(): void {
        this.tabs.focusChange
            .subscribe(event => {
                if (event.tab.textLabel === 'Users') {
                    if (this.usersDataSource == null) {
                        this.httpClient.get(ApiUrls.users)
                            .subscribe((response: User[]) => this.usersDataSource =
                                new MatTableDataSource<User>(response));
                    }
                } else if (event.tab.textLabel === 'Orders') {
                    if (this.ordersDataSource == null) {
                        this.httpClient.get(ApiUrls.orders)
                            .subscribe((response: Order[]) => this.ordersDataSource =
                                new MatTableDataSource<Order>(response));
                    }
                }
            });

        this.tabs._focusChanged(0);
    }
}