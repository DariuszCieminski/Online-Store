import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { ApiUrls } from "../main/util/api-urls";
import { HttpClient } from "@angular/common/http";
import { MatTabGroup } from "@angular/material/tabs";
import { MatTableDataSource } from "@angular/material/table";
import { MatPaginator } from "@angular/material/paginator";

@Component({
    selector: 'app-admin',
    templateUrl: './admin.component.html',
    styleUrls: ['./admin.component.css']
})
export class AdminComponent implements AfterViewInit {
    @ViewChild(MatTabGroup) tabs: MatTabGroup;
    @ViewChild(MatPaginator) paginator: MatPaginator;

    dataSource: MatTableDataSource<any>[] = new Array(2);
    apiUrls: string[] = [ApiUrls.users, ApiUrls.orders];
    displayedColumns: string[][] = [
        ['name', 'surname', 'email', 'address'],
        ['id', 'buyer', 'items', 'deliveryAddress', 'paymentMethod', 'status', 'cost', 'time', 'information']
    ];

    constructor(private httpClient: HttpClient) {
    }

    ngAfterViewInit(): void {
        this.tabs.focusChange.subscribe(event => {
            const index = event.index;
            if (this.dataSource[index] == null) {
                this.httpClient.get(this.apiUrls[index])
                    .subscribe((response: any[]) => this.addDataSource(index, response));
            } else {
                this.dataSource[index].filter = null;
            }
        });
        this.tabs._focusChanged(0);
    }

    addDataSource<T>(index: number, response: T[]): void {
        this.dataSource[index] = new MatTableDataSource<T>(response);
        this.dataSource[index].paginator = this.paginator;
    }

    onSearch(event: EventTarget): void {
        this.dataSource[this.tabs.selectedIndex].filter = (event as HTMLInputElement).value;
    }
}