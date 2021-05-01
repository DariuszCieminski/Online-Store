import { AfterViewInit, Directive, Input, OnChanges, SimpleChanges, ViewChild } from '@angular/core';
import { MatTableDataSource } from "@angular/material/table";
import { MatSort } from "@angular/material/sort";

@Directive()
export abstract class AbstractTableComponent<T> implements OnChanges, AfterViewInit {
    @Input() dataSource: MatTableDataSource<T>;
    @Input() displayedColumns: string[];
    @ViewChild(MatSort) sort: MatSort;

    setSort(): void {
        if (this.dataSource != null && this.dataSource.sort == null) {
            this.dataSource.sort = this.sort;
        }
    }

    ngAfterViewInit(): void {
        this.setSort();
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.setSort();
    }
}