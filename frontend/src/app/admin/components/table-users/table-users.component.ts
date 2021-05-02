import { Component } from '@angular/core';
import { HttpErrorResponse } from "@angular/common/http";
import { MatDialog } from "@angular/material/dialog";
import { Observable, of } from "rxjs";
import { AbstractTableComponent } from "../../../shared/components/abstract-table/abstract-table.component";
import { UserDataComponent } from "../user-data/user-data.component";
import { DialogDeleteComponent } from "../../../shared/components/dialog-delete.component";
import { UserDetailed } from "../../models/user-detailed";
import { UserRole } from "../../models/user-role.enum";
import { UserService } from "../../../main/services/user.service";
import { SnackbarService } from "../../../main/services/snackbar.service";

@Component({
    selector: 'table-users',
    templateUrl: './table-users.component.html',
    styleUrls: ['./table-users.component.css', '../../../shared/components/abstract-table/abstract-table.component.css']
})
export class TableUsersComponent extends AbstractTableComponent<UserDetailed> {
    constructor(private matDialog: MatDialog, private userService: UserService, private snackBar: SnackbarService) {
        super();
    }

    printUserRole(role: string): UserRole {
        return UserRole[role];
    }

    showEditDialog(user: UserDetailed): void {
        this.matDialog.open(UserDataComponent, {data: user}).afterClosed()
            .subscribe(value => {
                if (value) {
                    this.userService.updateUser({...user, ...value})
                        .subscribe(response => this.handleSuccess(
                            response, 'edit', `User with ID: ${response['id']} was successfully updated.`
                        ), (error) => this.handleError(error));
                }
            });
    }

    showDeleteDialog(user: UserDetailed): void {
        this.matDialog.open(DialogDeleteComponent, {
            data: user.id,
            disableClose: true,
            width: '30%'
        }).afterClosed()
            .subscribe(value => {
                if (value) {
                    this.userService.deleteUser(user.id)
                        .subscribe(() => this.handleSuccess(
                            user, 'delete', "User was removed successfully."
                        ), (error) => this.handleError(error));
                }
            });
    }

    handleSuccess(data: UserDetailed, action: string, message: string): void {
        this.updateTable(data, action);
        this.snackBar.show(message);
    }

    handleError(error: HttpErrorResponse): Observable<any> {
        return of(this.snackBar.show(
            `Error ${error.status}: ${error.error}`, 'OK', {duration: 0}
        ));
    }

    updateTable(data: UserDetailed, action: string) {
        const index = this.dataSource.data.findIndex(user => user.id === data.id);

        if (action === 'edit') {
            this.dataSource.data[index] = data;
        } else if (action === 'delete') {
            this.dataSource.data.splice(index, 1);
        }

        this.dataSource._updateChangeSubscription();
    }
}