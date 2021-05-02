import { AbstractControl, Validators } from "@angular/forms";
import { Validator } from "../../main/util/validator";

export class Utilities {
    static setEventForAddressChange(address: AbstractControl): void {
        address.valueChanges
            .subscribe(value => {
                if (value.street || value.postCode || value.city) {
                    address.get('street').setValidators(Validators.required);
                    address.get('postCode').setValidators([Validators.required, Validator.postCode]);
                    address.get('city').setValidators(Validators.required);
                } else {
                    address.get('street').clearValidators();
                    address.get('postCode').clearValidators();
                    address.get('city').clearValidators();
                }
                address.get('street').updateValueAndValidity({emitEvent: false});
                address.get('postCode').updateValueAndValidity({emitEvent: false});
                address.get('city').updateValueAndValidity({emitEvent: false});
            });
    }
}