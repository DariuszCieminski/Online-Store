import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { SnackbarService } from "../../services/snackbar.service";
import { ApiUrls } from "../../util/api-urls";
import { Validator } from "../../util/validator";
import { Utilities } from "../../../shared/util/utilities";

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

        Utilities.setEventForAddressChange(this.formArray.get([2]));
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
                            .then(() => this.snackBar.show("Your account was successfully created. You can now log in."));
                    },
                    error => {
                        this.formSubmitted = false;
                        this.handleError(error);
                    }
                );
        }
    }

    private handleError(errorResponse: HttpErrorResponse): void {
        //map error messages to a list
        const errors: string[] = errorResponse.error["errors"].map(value => value.error);
        const message = errors.reduce((previousValue, currentValue) => `${previousValue}\n${currentValue}`);
        this.snackBar.show(message, "Close", {panelClass: 'snackbar'});
    }
}