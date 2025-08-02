// src/main.ts
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { importProvidersFrom } from '@angular/core';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { AuthInterceptor } from './app/auth.interceptor';
import { provideRouter, Routes } from '@angular/router';
import { CourseDetailComponent } from './app/course-detail/course-detail.component';
import { CertificateVerificationComponent } from './app/certificate-verification/certificate-verification.component';
import { HomeComponent } from './app/home/home.component'; // NEW: Import HomeComponent
import { PaymentComponent } from './app/payment/payment.component';

// Define your application routes
const routes: Routes = [
  { path: '', component: HomeComponent }, // Default route loads HomeComponent
  { path: 'login', component: HomeComponent }, // Route for login form (HomeComponent handles display)
  { path: 'register', component: HomeComponent }, // Route for register form (HomeComponent handles display)
  { path: 'my-courses', component: HomeComponent }, // Route for my courses (HomeComponent handles display)
  { path: 'courses/:id', component: CourseDetailComponent }, // Route for course details
  { path: 'payment/:id', component: PaymentComponent },
  { path: 'verify-certificate/:userId/:courseId', component: CertificateVerificationComponent },
  { path: '**', redirectTo: '' } // Redirect any unknown path to home
];

bootstrapApplication(AppComponent, {
  providers: [
    importProvidersFrom(HttpClientModule),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    provideRouter(routes)
  ]
})
  .catch(err => console.error(err));
