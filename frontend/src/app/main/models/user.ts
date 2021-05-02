import { Address } from "./address";

export interface User {
    name: string;
    surname: string;
    email: string;
    address: Address;
}