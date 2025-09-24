// src/app/register/register.component.ts
import { Component, DestroyRef, EventEmitter, OnDestroy, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService, RegisterRequest } from '../auth.service';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { AppState } from '../store/app.state';
import * as AuthActions from '../store/auth/auth.actions';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent implements OnDestroy {
  registerRequest: RegisterRequest = { username: '', email: '', password: '' };
  errorMessage = '';
  successMessage = '';

  @Output() switchToLogin = new EventEmitter<void>();
  @Output() registerSuccess = new EventEmitter<void>();

  private readonly destroy$ = new Subject<void>();
  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private authService: AuthService,
    private router: Router,
    private store: Store<AppState>
  ) {
    this.destroyRef.onDestroy(() => {
      this.destroy$.next();
      this.destroy$.complete();
    });
  }

  ngOnDestroy(): void {
    // Cleanup handled by destroyRef
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.register(this.registerRequest)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('Registration successful:', response);
          // Automatically log in after successful registration
          this.store.dispatch(AuthActions.login({
            username: this.registerRequest.username,
            password: this.registerRequest.password
          }));
          this.registerSuccess.emit();
        },
        error: (err) => {
          console.error('Registration failed:', err);
          this.errorMessage = 'Registration failed. Please try again.';
          if (err.error && err.error.message) {
            this.errorMessage = err.error.message;
        }
      }
    });
  }

  onSwitchToLogin(): void {
    this.switchToLogin.emit();
  }
}
