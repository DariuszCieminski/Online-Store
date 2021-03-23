import { HTTP_INTERCEPTORS, HttpClient, HttpXsrfTokenExtractor } from "@angular/common/http";
import { Router } from "@angular/router";
import { NgxPermissionsService } from "ngx-permissions";
import { AuthenticationGuard } from "../authentication/authentication-guard";
import { AuthenticationService } from "../authentication/authentication-service";
import { HttpXsrfInterceptor } from "../authentication/session/http-xsrf-interceptor";
import { SessionAuthenticationGuard } from "../authentication/session/session-authentication-guard";
import { SessionAuthenticationService } from "../authentication/session/session-authentication.service";

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