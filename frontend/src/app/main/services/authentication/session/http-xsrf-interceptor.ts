import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpXsrfTokenExtractor } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable()
export class HttpXsrfInterceptor implements HttpInterceptor {
    private readonly HEADER_NAME: string = "X-XSRF-TOKEN";

    constructor(private tokenExtractor: HttpXsrfTokenExtractor) {
    }

    intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        if (request.method.match(/(GET|HEAD)/g)) {
            return next.handle(request.clone({withCredentials: true}));
        }

        const token = this.tokenExtractor.getToken();
        let requestHeaders = request.headers;

        if (token !== null && !requestHeaders.has(this.HEADER_NAME)) {
            requestHeaders = requestHeaders.set(this.HEADER_NAME, token);
        }
        return next.handle(request.clone({headers: requestHeaders, withCredentials: true}));
    }
}