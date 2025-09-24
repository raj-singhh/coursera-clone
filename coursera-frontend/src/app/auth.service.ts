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

// Export MessageResponse interface
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

  // Observable for current user info
  private currentUserSubject = new BehaviorSubject<{ username: string; id: number | null; email: string | null } | null>(this.getCurrentUserInfo());
  currentUser$ = this.currentUserSubject.asObservable();

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
    console.log('Making login request to:', `${this.authApiUrl}/login`);
    console.log('With payload:', request);
    return this.http.post<JwtResponse>(`${this.authApiUrl}/login`, request).pipe(
      tap(response => {
        console.log('Received login response:', response);
        localStorage.setItem('jwt_token', response.token);
        localStorage.setItem('user_id', response.id.toString());
        localStorage.setItem('username', response.username);
        localStorage.setItem('email', response.email);
        this.setLoggedIn(true);
        this.currentUserSubject.next({ username: response.username, id: response.id, email: response.email });
      }),
      catchError(error => {
        console.error('Login error:', error);
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
    this.currentUserSubject.next(null);
  }
  // Helper to get current user info from localStorage
  private getCurrentUserInfo(): { username: string; id: number | null; email: string | null } | null {
    const username = localStorage.getItem('username');
    const id = localStorage.getItem('user_id');
    const email = localStorage.getItem('email');
    if (username && id) {
      return { username, id: parseInt(id, 10), email };
    }
    return null;
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
