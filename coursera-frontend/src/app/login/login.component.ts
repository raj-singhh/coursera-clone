// src/app/login/login.component.ts
import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService, LoginRequest } from '../auth.service';
import { Router } from '@angular/router'; // Import Router

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  loginRequest: LoginRequest = { username: '', password: '' };
  errorMessage: string = '';

  @Output() switchToRegister = new EventEmitter<void>();
  @Output() loginSuccess = new EventEmitter<void>(); // Emit event to notify parent of successful login

  constructor(private authService: AuthService, private router: Router) { }

  onSubmit(): void {
    this.errorMessage = '';
    this.authService.login(this.loginRequest).subscribe({
      next: (response) => {
        console.log('Login successful:', response);
        this.loginSuccess.emit(); // Emit event
      },
      error: (err) => {
        console.error('Login failed:', err);
        this.errorMessage = 'Login failed. Please check your username and password.';
        if (err.error && err.error.message) {
          this.errorMessage = err.error.message;
        }
      }
    });
  }

  onSwitchToRegister(): void {
    this.switchToRegister.emit();
  }
}
