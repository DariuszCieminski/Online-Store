import { Component } from '@angular/core';
import { AbstractTableComponent } from "../abstract-table/abstract-table.component";
import { MatDialog } from "@angular/material/dialog";
import { MatRow } from "@angular/material/table";
import { HttpClient } from "@angular/common/http";
import { OrderStatusChangerComponent } from "../../../admin/components/order-status-changer/order-status-changer.component";
import { Order } from "../../../main/models/order";
import { PaymentMethod } from "../../../main/models/payment-method.enum";
import { OrderStatus } from "../../../main/models/order-status.enum";
import { SnackbarService } from "../../../main/services/snackbar.service";
import { OrderService } from "../../../main/services/order.service";
import { NgxPermissionsService } from "ngx-permissions";

@Component({
    selector: 'table-orders',
    templateUrl: './table-orders.component.html',
    styleUrls: ['./table-orders.component.css', '../abstract-table/abstract-table.component.css']
})
export class TableOrdersComponent extends AbstractTableComponent<Order> {
    constructor(private dialog: MatDialog, private httpClient: HttpClient, private snackBar: SnackbarService,
                private orderService: OrderService, private permissions: NgxPermissionsService) {
        super();
    }

    getPaymentMethod(key: string): string {
        return PaymentMethod[key];
    }

    getOrderStatus(key: string): string {
        return OrderStatus[key];
    }

    handleDoubleClick(order: MatRow): void {
        if (this.permissions.getPermissions().hasOwnProperty("MANAGER")) {
            this.showStatusChangeDialog(order);
        }
    }

    showStatusChangeDialog(order: MatRow): void {
        this.dialog.open(OrderStatusChangerComponent, {data: order["status"]}).afterClosed()
            .subscribe(status => {
                if (status === undefined) return;
                const id = order["id"];
                this.orderService.modifyOrderStatus(id, status)
                    .subscribe(() => {
                        this.snackBar.showSnackbar(
                            `Status for the order of id: ${id} was changed to ${this.getOrderStatus(status)}.`);
                    }, () => this.snackBar.showSnackbar("An error occurred."));
            });
    }
}