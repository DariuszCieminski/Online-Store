import Big from "big.js";
import { Address } from "./address";
import { OrderItem } from "./order-item";
import { OrderStatus } from "./order-status.enum";
import { PaymentMethod } from "./payment-method.enum";
import { User } from "./user";

export class Order {
    readonly id: number;
    buyer: User;
    items: OrderItem[];
    deliveryAddress: Address;
    paymentMethod: PaymentMethod;
    information: string;
    status: OrderStatus;
    readonly time: string;
    readonly cost: Big

    constructor(items: OrderItem[], deliveryAddress: Address, paymentMethod: PaymentMethod, information: string) {
        this.items = items;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.information = information;
    }
}