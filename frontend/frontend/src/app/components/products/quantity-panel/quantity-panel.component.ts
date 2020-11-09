import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, ValidationErrors, Validators } from "@angular/forms";

@Component({
    selector: 'quantity-panel',
    templateUrl: './quantity-panel.component.html',
    styleUrls: ['./quantity-panel.component.css']
})
export class QuantityPanelComponent implements OnInit {
    quantity: FormControl;
    @Input() min: number = 1;
    @Input() max: number = 0;
    @Input() value: number = this.min;
    @Output() valueChanged = new EventEmitter<number>();
    @Output() onError = new EventEmitter<ValidationErrors>();

    constructor(private changeDetector: ChangeDetectorRef) {
    }

    ngOnInit(): void {
        this.quantity = new FormControl(this.value,
            [Validators.required, Validators.min(this.min), Validators.max(this.max)]);

        if (!this.max) {
            this.quantity.disable();
            this.quantity.setErrors({'empty': true});
            this.onError.emit(this.quantity.errors);
        }

        this.changeDetector.detectChanges();
    }

    onValueChanged(newValue?: number): void {
        if (newValue) this.quantity.setValue(newValue);
        if (this.quantity.errors) {
            this.onError.emit(this.quantity.errors);
        } else {
            this.valueChanged.emit(this.quantity.value);
        }
    }
}