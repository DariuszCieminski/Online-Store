import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTabsModule } from "@angular/material/tabs";
import { MatTableModule } from "@angular/material/table";
import { FlexLayoutModule } from "@angular/flex-layout";
import { SharedModule } from "../shared/shared.module";
import { MatSortModule } from "@angular/material/sort";
import { MatPaginatorModule } from "@angular/material/paginator";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { ReactiveFormsModule } from "@angular/forms";
import { MatSelectModule } from "@angular/material/select";
import { MatButtonModule } from "@angular/material/button";
import { AdminComponent } from "./admin.component";
import { TableUsersComponent } from './components/table-users/table-users.component';
import { OrderStatusChangerComponent } from './components/order-status-changer/order-status-changer.component';
import { UserDataComponent } from './components/user-data/user-data.component';

@NgModule({
    declarations: [
        AdminComponent,
        TableUsersComponent,
        OrderStatusChangerComponent,
        UserDataComponent
    ],
    imports: [
        CommonModule,
        MatTabsModule,
        MatTableModule,
        FlexLayoutModule,
        SharedModule,
        MatSortModule,
        MatPaginatorModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        ReactiveFormsModule,
        MatSelectModule,
        MatButtonModule
    ]
})
export class AdminModule {
}