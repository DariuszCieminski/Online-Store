import { AbstractControl, ValidationErrors } from "@angular/forms";

export class Validator {
    static passwordMatch(control: AbstractControl): ValidationErrors | null {
        const pwd = control.get("password").value;
        const repeatPwd = control.get("repeatPassword").value;
        return pwd === repeatPwd ? null : {"password_mismatch": true};
    }

    static postCode(control: AbstractControl): ValidationErrors | null {
        return RegExp("^[0-9]{2}-[0-9]{3}$").test(control.value) ? null : {'postCode': true};
    }

    static imageUrl(control: AbstractControl): ValidationErrors | null {
        return RegExp("^(http|https)?.{0,50}/.{0,200}").test(control.value) ? null : {'imageUrl': true};
    }

    static price(control: AbstractControl): ValidationErrors | null {
        if (control.value == null) return null;
        return RegExp("^\\d{1,10}([,.]\\d{1,2})?$").test(control.value) ? null : {'price': true};
    }

    static priceRange(control: AbstractControl): ValidationErrors | null {
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