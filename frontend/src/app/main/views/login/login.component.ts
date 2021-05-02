import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { AuthenticationService } from "../../services/authentication/authentication-service";
import { SnackbarService } from "../../services/snackbar.service";

@Component({
    selector: 'login-form',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
    form: FormGroup;
    isAuthenticating: boolean;

    constructor(private authService: AuthenticationService, private router: Router, private snackBar: SnackbarService) {
    }

    ngOnInit(): void {
        this.isAuthenticating = false;
        this.form = new FormGroup({
            email: new FormControl('', [Validators.email, Validators.required]),
            password: new FormControl('', Validators.required)
        });
    }

    onLogin() {
        if (this.form.valid) {
            this.isAuthenticating = true;
            this.authService.login(this.form.value)
                .subscribe(success => {
                    if (success) {
                        return this.router.navigateByUrl('/');
                    } else {
                        this.isAuthenticating = false;
                        this.snackBar.show("Invalid email or password", "Close");
                    }
                });
        }
    }
}