// src/app/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { environment } from '../environments/environment';

// DTOs for frontend (mirroring backend)
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface JwtResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
}

// NEW: Export MessageResponse interface
export interface MessageResponse {
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authApiUrl = `${environment.backendUrl}/api/auth`;
  private isLoggedInSubject = new BehaviorSubject<boolean>(this.hasToken());

  isLoggedIn$ = this.isLoggedInSubject.asObservable();

  constructor(private http: HttpClient) { }

  private hasToken(): boolean {
    return !!localStorage.getItem('jwt_token');
  }

  private setLoggedIn(value: boolean): void {
    this.isLoggedInSubject.next(value);
  }

  register(request: RegisterRequest): Observable<any> {
    return this.http.post(`${this.authApiUrl}/register`, request);
  }

  login(request: LoginRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.authApiUrl}/login`, request).pipe(
      tap(response => {
        localStorage.setItem('jwt_token', response.token);
        localStorage.setItem('user_id', response.id.toString());
        localStorage.setItem('username', response.username);
        localStorage.setItem('email', response.email);
        this.setLoggedIn(true);
      }),
      catchError(error => {
        this.logout();
        throw error;
      })
    );
  }

  logout(): void {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_id');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    this.setLoggedIn(false);
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

  getUserId(): number | null {
    const userId = localStorage.getItem('user_id');
    return userId ? parseInt(userId, 10) : null;
  }

  getUsername(): string | null {
    return localStorage.getItem('username');
  }

  getEmail(): string | null {
    return localStorage.getItem('email');
  }

  isUserLoggedIn(): boolean {
    return this.hasToken();
  }
}
