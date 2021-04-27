import { AfterViewInit, Component } from '@angular/core';
import { AbstractTableComponent } from "../../../shared/components/abstract-table/abstract-table.component";
import { User } from "../../../main/models/user";

@Component({
    selector: 'table-users',
    templateUrl: './table-users.component.html',
    styleUrls: ['./table-users.component.css', '../../../shared/components/abstract-table/abstract-table.component.css']
})
export class TableUsersComponent extends AbstractTableComponent<User> implements AfterViewInit {

}