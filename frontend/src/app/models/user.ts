import { Address } from "./address";

export class User {
    id: number;
    name: string;
    surname: string;
    email: string;
    address: Address;
    roles: string[];
}