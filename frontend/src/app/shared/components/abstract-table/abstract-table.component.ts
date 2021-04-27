import { AfterViewInit, Directive, Input, ViewChild } from '@angular/core';
import { MatTableDataSource } from "@angular/material/table";
import { MatSort } from "@angular/material/sort";

@Directive()
export abstract class AbstractTableComponent<T> implements AfterViewInit {
    @Input() dataSource: MatTableDataSource<T>;
    @Input() displayedColumns: string[];
    @ViewChild(MatSort) sort: MatSort;

    ngAfterViewInit(): void {
        if (this.dataSource != null) {
            this.dataSource.sort = this.sort;
        }
    }
}