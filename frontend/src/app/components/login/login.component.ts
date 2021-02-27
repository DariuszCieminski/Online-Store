import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { AuthenticationService } from "../../../authentication/authentication.service";
import { SnackbarService } from "../../services/snackbar.service";

@Component({
    selector: 'login-form',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
    form: FormGroup;

    constructor(private authService: AuthenticationService, private router: Router, private snackBar: SnackbarService) {
    }

    ngOnInit(): void {
        this.form = new FormGroup({
            email: new FormControl('', [Validators.email, Validators.required]),
            password: new FormControl('', Validators.required)
        });

        if (history.state.register) {
            this.snackBar.showSnackbar("Your account was successfully created. You can now log in.");
        }
    }

    onLogin() {
        if (this.form.valid) {
            this.authService.login(this.form.value)
                .subscribe(success => {
                    (success) ? this.router.navigateByUrl('/')
                              : this.snackBar.showSnackbar("Invalid email or password", "Close");
                });
        }
    }
}