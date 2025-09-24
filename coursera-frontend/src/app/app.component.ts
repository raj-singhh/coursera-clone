// src/app/app.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, Router, RouterLinkActive } from '@angular/router';
import { AuthService } from './auth.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive
  ],
})
export class AppComponent implements OnInit {
  title = 'Coursera Clone';
  isLoggedIn$: Observable<boolean>;
  username: string = '';

  constructor(
    private authService: AuthService,
    public router: Router
  ) {
    this.isLoggedIn$ = this.authService.isLoggedIn$;
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.username = user?.username || '';
    });
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
