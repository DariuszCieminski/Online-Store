import { DatePipe } from "@angular/common";
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'customDate'
})
export class CustomDatePipe extends DatePipe implements PipeTransform {
    transform(value: any): string | null {
        return super.transform(value, 'dd/MM/yyyy HH:mm:ss');
    }
}