import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { AuthenticationService } from "../../services/authentication.service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Router } from "@angular/router";

@Component({
    selector: 'login-form',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
    form: FormGroup;

    constructor(private authService: AuthenticationService, private router: Router, private snackBar: MatSnackBar) {
    }

    ngOnInit(): void {
        this.form = new FormGroup({
            email: new FormControl('', [Validators.email, Validators.required]),
            password: new FormControl('', Validators.required)
        });

        if (history.state.register) {
            this.showSnackBar("Your account was successfully created. You can now log in.", null);
        }
    }

    onLogin() {
        if (this.form.valid) {
            this.authService.login(this.form.value)
                .subscribe(success => {
                    if (success) this.router.navigateByUrl('/');
                    else this.showSnackBar("Invalid email or password", "Close");
                });
        }
    }

    showSnackBar(message: string, action: string | null) {
        this.snackBar.open(message, action, {
            duration: 3000,
            horizontalPosition: "center",
            verticalPosition: "bottom"
        });
    }
}