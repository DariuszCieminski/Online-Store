import { AfterViewInit, Component } from '@angular/core';
import { PaymentMethod } from "../../../main/models/payment-method.enum";
import { OrderStatus } from "../../../main/models/order-status.enum";
import { AbstractTableComponent } from "../abstract-table/abstract-table.component";
import { Order } from "../../../main/models/order";

@Component({
    selector: 'table-orders',
    templateUrl: './table-orders.component.html',
    styleUrls: ['./table-orders.component.css', '../abstract-table/abstract-table.component.css']
})
export class TableOrdersComponent extends AbstractTableComponent<Order> implements AfterViewInit {
    getPaymentMethod(key: string): string {
        return PaymentMethod[key];
    }

    getOrderStatus(key: string): string {
        return OrderStatus[key];
    }
}