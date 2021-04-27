import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTabsModule } from "@angular/material/tabs";
import { MatTableModule } from "@angular/material/table";
import { MatSortModule } from "@angular/material/sort";
import { FlexLayoutModule } from "@angular/flex-layout";
import { TableOrdersComponent } from "./components/table-orders/table-orders.component";
import { CurrencyPLNPipe } from "./pipes/currency-pln.pipe";
import { CustomDatePipe } from "./pipes/custom-date.pipe";

@NgModule({
    declarations: [
        TableOrdersComponent,
        CurrencyPLNPipe,
        CustomDatePipe,
        TableOrdersComponent
    ],
    imports: [
        CommonModule,
        MatTabsModule,
        MatTableModule,
        MatSortModule,
        FlexLayoutModule
    ],
    exports: [
        TableOrdersComponent,
        CurrencyPLNPipe,
        CustomDatePipe
    ]
})
export class SharedModule {
}