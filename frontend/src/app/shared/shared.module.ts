import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTabsModule } from "@angular/material/tabs";
import { MatTableModule } from "@angular/material/table";
import { MatSortModule } from "@angular/material/sort";
import { FlexLayoutModule } from "@angular/flex-layout";
import { MatIconModule } from "@angular/material/icon";
import { MatButtonModule } from "@angular/material/button";
import { MatDialogModule } from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { TableOrdersComponent } from "./components/table-orders/table-orders.component";
import { DialogDeleteComponent } from "./components/dialog-delete.component";
import { CurrencyPLNPipe } from "./pipes/currency-pln.pipe";
import { CustomDatePipe } from "./pipes/custom-date.pipe";

@NgModule({
    declarations: [
        TableOrdersComponent,
        DialogDeleteComponent,
        CurrencyPLNPipe,
        CustomDatePipe
    ],
    imports: [
        CommonModule,
        MatTabsModule,
        MatTableModule,
        MatSortModule,
        FlexLayoutModule,
        MatIconModule,
        MatButtonModule,
        MatFormFieldModule,
        MatDialogModule
    ],
    exports: [
        TableOrdersComponent,
        DialogDeleteComponent,
        CurrencyPLNPipe,
        CustomDatePipe
    ]
})
export class SharedModule {
}