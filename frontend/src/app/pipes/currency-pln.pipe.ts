import { CurrencyPipe } from "@angular/common";
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'currencyPLN'
})
export class CurrencyPLNPipe extends CurrencyPipe implements PipeTransform {
    transform(value: any): any {
        return super.transform(value, 'PLN', 'symbol', '1.2-2', 'pl');
    }
}