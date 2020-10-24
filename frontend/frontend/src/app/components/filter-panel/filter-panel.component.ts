import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { Validator } from "../../util/validator";

@Component({
    selector: 'filter-panel',
    templateUrl: './filter-panel.component.html',
    styleUrls: ['./filter-panel.component.css']
})
export class FilterPanelComponent implements OnInit {
    form: FormGroup;
    @Output() onFilter: EventEmitter<object> = new EventEmitter<object>();

    readonly errorMessages: object = {
        min: "Price must be greater than zero",
        price: "Price has wrong precision",
        priceFilter: "Price range is invalid"
    };

    constructor(private builder: FormBuilder) {
    }

    ngOnInit(): void {
        this.form = this.builder.group({
            descContains: new FormControl(),
            priceGreaterThan: new FormControl(null, [Validators.min(0), Validator.PriceValidator()]),
            priceLessThan: new FormControl(null, [Validators.min(0), Validator.PriceValidator()]),
            priceEqualTo: new FormControl(null, [Validators.min(0), Validator.PriceValidator()])
        }, {validators: Validator.PriceFilterValidator()});
    }

    onFilterSubmit(): void {
        if (this.form.valid) {
            let value = this.form.value;

            for (let key in value) {
                if (value.hasOwnProperty(key) && value[key] == null) {
                    delete value[key];
                }
            }
            this.onFilter.emit(value);
        }
    }
}