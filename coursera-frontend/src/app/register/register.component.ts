// src/app/register/register.component.ts
import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService, RegisterRequest } from '../auth.service';
import { Router } from '@angular/router'; // Import Router

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  registerRequest: RegisterRequest = { username: '', email: '', password: '' };
  errorMessage: string = '';
  successMessage: string = '';

  @Output() switchToLogin = new EventEmitter<void>();
  @Output() registerSuccess = new EventEmitter<void>(); // NEW: Emit event for successful registration

  constructor(private authService: AuthService, private router: Router) { }

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.register(this.registerRequest).subscribe({
      next: (response) => {
        console.log('Registration successful:', response);
        this.successMessage = 'Registration successful! You can now login.';
        this.registerRequest = { username: '', email: '', password: '' };
        this.registerSuccess.emit(); // Emit event
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
