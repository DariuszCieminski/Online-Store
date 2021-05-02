import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { UserDetailed } from "../../models/user-detailed";
import { FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { UserRole } from "../../models/user-role.enum";
import { Utilities } from "../../../shared/util/utilities";

@Component({
    selector: 'app-user-data',
    templateUrl: './user-data.component.html',
    styleUrls: ['./user-data.component.css', '../order-status-changer/order-status-changer.component.css']
})
export class UserDataComponent implements OnInit {
    userForm: FormGroup;

    constructor(@Inject(MAT_DIALOG_DATA) public data: UserDetailed,
                private dialogRef: MatDialogRef<UserDataComponent>,
                private builder: FormBuilder) {
    }

    ngOnInit(): void {
        this.userForm = this.builder.group(
            {
                name: new FormControl(this.data.name, Validators.required),
                surname: new FormControl(this.data.surname, Validators.required),
                email: new FormControl(this.data.email, [Validators.required, Validators.email]),
                address: new FormGroup(
                    {
                        street: new FormControl(this.data.address?.street),
                        postCode: new FormControl(this.data.address?.postCode),
                        city: new FormControl(this.data.address?.city)
                    }
                ),
                roles: new FormControl(this.data.roles)
            }
        );

        const address = this.userForm.get("address");
        Utilities.setEventForAddressChange(address);
    }

    getAllRoles(): string[] {
        return Object.keys(UserRole);
    }

    getRoleName(key: string): UserRole {
        return UserRole[key];
    }

    onSubmit(): void {
        if (this.userForm.valid) {
            let value = this.userForm.value;
            if (!value.address.street && !value.address.postCode && !value.address.city) {
                value.address = null;
            }
            this.dialogRef.close(value);
        }
    }
}