import { User } from "./user";
import { OrderItem } from "./order-item";
import { Address } from "./address";
import { PaymentMethod } from "./payment-method.enum";
import { OrderStatus } from "./order-status.enum";
import Big from "big.js";

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

    constructor(buyer: User, items: OrderItem[], deliveryAddress: Address, paymentMethod: PaymentMethod, information: string) {
        this.buyer = buyer;
        this.items = items;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.information = information;
    }
}