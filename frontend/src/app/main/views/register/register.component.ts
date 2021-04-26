import { HttpClient } from "@angular/common/http";
import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { SnackbarService } from "../../services/snackbar.service";
import { ApiUrls } from "../../util/api-urls";
import { Validator } from "../../util/validator";

@Component({
    selector: 'app-register',
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
    formGroup: FormGroup;
    formSubmitted: boolean;

    constructor(private builder: FormBuilder, private httpClient: HttpClient, private router: Router, private snackBar: SnackbarService) {
    }

    get formArray(): AbstractControl {
        return this.formGroup.get("formArray");
    }

    ngOnInit(): void {
        this.formSubmitted = false;
        this.formGroup = this.builder.group({
            formArray: this.builder.array([
                this.builder.group({
                    name: new FormControl('', Validators.required),
                    surname: new FormControl('', Validators.required),
                    email: new FormControl('', [Validators.email, Validators.required])
                }),
                this.builder.group({
                        password: new FormControl('', [Validators.required, Validators.minLength(8)]),
                        repeatPassword: new FormControl('', Validators.required)
                    },
                    {validators: Validator.passwordMatch}),
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
                    this.formArray.get([2]).get('postCode').setValidators([Validators.required, Validator.postCode]);
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
            delete data.repeatPassword;

            const address = this.formArray.get([2]).value;
            if (!address.street && !address.postCode && !address.city) data.address = null;
            else data.address = address;

            this.formSubmitted = true;
            this.httpClient.post(ApiUrls.users, data)
                .subscribe(() => {
                        this.router.navigateByUrl('/login')
                            .then(() => this.snackBar.showSnackbar("Your account was successfully created. You can now log in."));
                    },
                    error => {
                        this.formSubmitted = false;
                        this.handleError(error);
                    }
                );
        }
    }

    private handleError(errorResponse: any): void {
        const errors: string[] = errorResponse.error.errors.map(value => value.error);
        const message = errors.reduce((previousValue, currentValue) => `${previousValue}\n${currentValue}`);
        this.snackBar.showSnackbar(message, "Close", {panelClass: 'snackbar'});
    }
}