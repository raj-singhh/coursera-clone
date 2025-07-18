// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component'; // VERIFY THIS PATH
import { LoginComponent } from './login/login.component'; // VERIFY THIS PATH
import { RegisterComponent } from './register/register.component'; // VERIFY THIS PATH
import { CourseDetailComponent } from './course-detail/course-detail.component'; // VERIFY THIS PATH

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'course/:id', component: CourseDetailComponent },
  // Add any other specific routes you have here
  { path: '**', redirectTo: '' } // Redirect any unknown paths to the home page
];
