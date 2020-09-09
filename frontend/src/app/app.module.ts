import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { MatToolbarModule } from '@angular/material/toolbar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { HomeComponent } from './components/home/home.component';
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatIconModule } from "@angular/material/icon";
import { RouterModule } from "@angular/router";
import { MatButtonModule } from "@angular/material/button";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { MatStepperModule } from "@angular/material/stepper";
import { HTTP_INTERCEPTORS, HttpClientModule } from "@angular/common/http";
import { NgxPermissionsGuard, NgxPermissionsModule } from 'ngx-permissions';
import { RequestInterceptor } from "./services/request.interceptor";
import { AuthenticationGuard } from "./services/authentication.guard";

@NgModule({
    declarations: [
        AppComponent,
        NavbarComponent,
        LoginComponent,
        RegisterComponent,
        HomeComponent
    ],
    imports: [
        BrowserModule,
        HttpClientModule,
        RouterModule.forRoot([
            {
                path: "",
                component: HomeComponent,
                canActivate: [AuthenticationGuard],
                data: {canAnonymous: true}
            },
            {
                path: "login",
                component: LoginComponent,
                canActivate: [NgxPermissionsGuard],
                data: {permissions: {only: 'GUEST', redirectTo: '/'}}
            },
            {
                path: "register",
                component: RegisterComponent,
                canActivate: [NgxPermissionsGuard],
                data: {permissions: {only: 'GUEST', redirectTo: '/'}}
            }
        ]),
        NgxPermissionsModule.forRoot(),
        MatToolbarModule,
        BrowserAnimationsModule,
        FormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatIconModule,
        MatButtonModule,
        MatSnackBarModule,
        ReactiveFormsModule,
        MatStepperModule
    ],
    providers: [
        {
            provide: HTTP_INTERCEPTORS,
            useClass: RequestInterceptor,
            multi: true
        }
    ],
    bootstrap: [AppComponent]
})
export class AppModule {
}
