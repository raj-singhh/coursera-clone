// src/app/auth.interceptor.ts
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service'; // Assuming AuthService manages token

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken(); // Get the token from AuthService

    // If a token exists, clone the request and add the Authorization header
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}` // Add the Bearer token
        }
      });
      console.log('DEBUG (AuthInterceptor): Adding Authorization header for request:', request.url);
    } else {
      console.log('DEBUG (AuthInterceptor): No token found for request:', request.url);
    }

    return next.handle(request);
  }
}
