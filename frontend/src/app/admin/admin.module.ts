import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTabsModule } from "@angular/material/tabs";
import { MatTableModule } from "@angular/material/table";
import { FlexLayoutModule } from "@angular/flex-layout";
import { SharedModule } from "../shared/shared.module";
import { AdminComponent } from "./admin.component";
import { MatSortModule } from "@angular/material/sort";
import { TableUsersComponent } from './components/table-users/table-users.component';

@NgModule({
    declarations: [
        AdminComponent,
        TableUsersComponent
    ],
    imports: [
        CommonModule,
        MatTabsModule,
        MatTableModule,
        FlexLayoutModule,
        SharedModule,
        MatSortModule
    ]
})
export class AdminModule {
}