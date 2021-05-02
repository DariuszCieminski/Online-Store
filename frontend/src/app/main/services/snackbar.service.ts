import { Injectable } from '@angular/core';
import { MatSnackBar, MatSnackBarConfig, MatSnackBarRef } from "@angular/material/snack-bar";

@Injectable({
    providedIn: 'root'
})
export class SnackbarService {

    constructor(private snackBar: MatSnackBar) {
    }

    show(message: string, action?: string, options?: MatSnackBarConfig): MatSnackBarRef<any> {
        return this.snackBar.open(message, action, options);
    }
}