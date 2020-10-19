import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";

export class Validator {
    static passwordValidator(): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            const password = control.get("password").value;
            const repeat = control.get("repeatPassword").value;
            return password === repeat ? null : {"password_mismatch": true};
        }
    }

    static PriceValidator(): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            if (control.value == null) return null;
            return RegExp("^\\d{1,10}([,.]\\d{1,2})?$").test(control.value) ? null : {'price': true};
        };
    }

    static PriceFilterValidator(): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            let priceLess = control.get('priceLessThan');
            let priceGreater = control.get('priceGreaterThan');

            if (priceGreater.valid && priceGreater.value != null && priceLess.valid && priceLess.value != null) {
                if (+priceGreater.value > +priceLess.value) {
                    return {'priceFilter': true};
                }
            }

            return null;
        }
    }
}