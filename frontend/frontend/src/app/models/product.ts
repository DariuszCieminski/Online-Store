import Big from "big.js";

export class Product {
    id: number;
    name: string;
    description: string;
    images: string[];
    price: Big;
    quantity: number;
}