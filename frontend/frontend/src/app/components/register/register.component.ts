import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormControl, FormGroup, ValidatorFn, Validators } from "@angular/forms";
import { HttpClient } from "@angular/common/http";
import { Router } from "@angular/router";

@Component({
    selector: 'app-register',
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
    formGroup: FormGroup;

    constructor(private builder: FormBuilder, private httpClient: HttpClient, private router: Router) {
    }

    get formArray(): AbstractControl {
        return this.formGroup.get("formArray");
    }

    ngOnInit(): void {
        this.formGroup = this.builder.group({
            formArray: this.builder.array([
                this.builder.group({
                    name: new FormControl('', Validators.required),
                    surname: new FormControl('', Validators.required),
                    email: new FormControl('', Validators.compose([Validators.email, Validators.required]))
                }),
                this.builder.group({
                        password: new FormControl('', [Validators.required, Validators.minLength(8)]),
                        repeatPassword: new FormControl('', Validators.required)
                    },
                    {validators: this.passwordValidator()}),
                this.builder.group({
                    street: new FormControl(''),
                    postCode: new FormControl(''),
                    city: new FormControl('')
                })
            ])
        });

        this.formArray.get([2]).valueChanges
            .subscribe(value => {
                if (value.street || value.postCode || value.city) {
                    this.formArray.get([2]).get('street').setValidators(Validators.required);
                    this.formArray.get([2]).get('postCode').setValidators([Validators.required, Validators.pattern("^[0-9]{2}-[0-9]{3}$")]);
                    this.formArray.get([2]).get('city').setValidators(Validators.required);
                } else {
                    this.formArray.get([2]).get('street').clearValidators();
                    this.formArray.get([2]).get('postCode').clearValidators();
                    this.formArray.get([2]).get('city').clearValidators();
                }
                this.formArray.get([2]).get('street').updateValueAndValidity({emitEvent: false});
                this.formArray.get([2]).get('postCode').updateValueAndValidity({emitEvent: false});
                this.formArray.get([2]).get('city').updateValueAndValidity({emitEvent: false});
            });
    }

    onSubmit(): void {
        if (this.formGroup.valid) {
            let data = Object.assign({}, this.formArray.get([0]).value, this.formArray.get([1]).value);
            data.roles = ['USER'];
            delete data.repeatPassword;

            const address = this.formArray.get([2]).value;
            if (!address.street && !address.postCode && !address.city) data.address = null;
            else data.address = address;

            this.httpClient.post('http://localhost:8080/api/users', data)
                .subscribe(() => {
                    this.router.navigateByUrl('/login', {state: {register: true}});
                });
        }
    }

    private passwordValidator(): ValidatorFn {
        return (control: AbstractControl): { [key: string]: boolean } | null => {
            const password = control.get("password").value;
            const repeat = control.get("repeatPassword").value;
            return password === repeat ? null : {"password_mismatch": true};
        }
    }
}