// src/app/app.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, Router } from '@angular/router'; // Import Router
import { AuthService } from './auth.service'; // Import AuthService
import { Observable } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet, // Essential for routing
    // REMOVED: RouterLink is not used directly in app.component.html anymore
  ]
})
export class AppComponent implements OnInit {
  title = 'Coursera Clone';
  isLoggedIn$: Observable<boolean>;
  username: string | null = null;

  // FIXED: Changed 'private' to 'public' so it can be accessed in the template
  constructor(
    private authService: AuthService,
    public router: Router // Make router public
  ) {
    this.isLoggedIn$ = this.authService.isLoggedIn$;
  }

  ngOnInit(): void {
    this.updateLoggedInUser();
    this.isLoggedIn$.subscribe(loggedIn => {
      this.updateLoggedInUser();
    });
  }

  private updateLoggedInUser(): void {
    this.username = this.authService.getUsername();
  }

  navigateToAllCourses(): void {
    this.router.navigate(['/']);
  }

  navigateToMyCourses(): void {
    if (!this.authService.getToken()) {
      alert('Please log in to view your courses.');
      this.router.navigate(['/login']);
      return;
    }
    this.router.navigate(['/my-courses']);
  }

  navigateToLogin(): void {
    this.router.navigate(['/login']);
  }

  navigateToRegister(): void {
    this.router.navigate(['/register']);
  }

  onLogout(): void {
    this.authService.logout();
    console.log('User logged out.');
    this.router.navigate(['/']);
  }
}
