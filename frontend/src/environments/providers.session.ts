import { HTTP_INTERCEPTORS, HttpClient, HttpXsrfTokenExtractor } from "@angular/common/http";
import { Router } from "@angular/router";
import { NgxPermissionsService } from "ngx-permissions";
import { AuthenticationGuard } from "../app/main/services/authentication/authentication-guard";
import { AuthenticationService } from "../app/main/services/authentication/authentication-service";
import { HttpXsrfInterceptor } from "../app/main/services/authentication/session/http-xsrf-interceptor";
import { SessionAuthenticationGuard } from "../app/main/services/authentication/session/session-authentication-guard";
import { SessionAuthenticationService } from "../app/main/services/authentication/session/session-authentication.service";

export const PROVIDERS = [
    {
        provide: AuthenticationService,
        useClass: SessionAuthenticationService,
        deps: [HttpClient, NgxPermissionsService]
    },
    {
        provide: AuthenticationGuard,
        useClass: SessionAuthenticationGuard,
        deps: [SessionAuthenticationService, Router]
    },
    {
        provide: HTTP_INTERCEPTORS,
        useClass: HttpXsrfInterceptor,
        deps: [HttpXsrfTokenExtractor],
        multi: true
    }
];