import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { OrderStatus } from "../../../main/models/order-status.enum";

@Component({
    selector: 'app-order-status-changer',
    templateUrl: './order-status-changer.component.html',
    styleUrls: ['./order-status-changer.component.css']
})
export class OrderStatusChangerComponent {

    constructor(@Inject(MAT_DIALOG_DATA) public data: OrderStatus,
                private dialogRef: MatDialogRef<OrderStatusChangerComponent>) {
    }

    getStatusList(): string[] {
        return Object.keys(OrderStatus);
    }

    getStatusName(key: string): OrderStatus {
        return OrderStatus[key];
    }

    onSubmit(newStatus: OrderStatus): void {
        this.dialogRef.close(newStatus);
    }
}