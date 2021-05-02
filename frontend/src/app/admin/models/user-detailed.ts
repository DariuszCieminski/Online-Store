import { User } from "../../main/models/user";
import { UserRole } from "./user-role.enum";

export interface UserDetailed extends User {
    readonly id: number,
    roles: UserRole[]
}