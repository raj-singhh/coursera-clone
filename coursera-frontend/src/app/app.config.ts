import { ApplicationConfig, importProvidersFrom, isDevMode } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule, provideHttpClient, withFetch, HTTP_INTERCEPTORS } from '@angular/common/http';
import { provideRouter, withComponentInputBinding, withRouterConfig, Routes } from '@angular/router';
import { provideStore } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';
import { Actions } from '@ngrx/effects';
import { AuthInterceptor } from './auth.interceptor';
import { AuthEffects } from './store/auth/auth.effects';
import { authReducer } from './store/auth/auth.reducer';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { CourseDetailComponent } from './course-detail/course-detail.component';
import { PaymentComponent } from './payment/payment.component';
import { CertificateVerificationComponent } from './certificate-verification/certificate-verification.component';

// Define your application routes
const routes: Routes = [
  { 
    path: '', 
    component: HomeComponent,
    pathMatch: 'full'
  },
  { 
    path: 'login', 
    component: LoginComponent,
    data: { title: 'Login' }
  },
  { 
    path: 'register', 
    component: RegisterComponent,
    data: { title: 'Register' }
  },
  { 
    path: 'my-courses', 
    component: HomeComponent,
    data: { requiresAuth: true }
  },
  { 
    path: 'courses/:id', 
    component: CourseDetailComponent,
    data: { title: 'Course Details' }
  },
  { 
    path: 'payment/:id', 
    component: PaymentComponent,
    data: { requiresAuth: true }
  },
  { 
    path: 'verify-certificate/:userId/:courseId', 
    component: CertificateVerificationComponent
  },
  { 
    path: 'privacy-policy', 
    component: HomeComponent,
    data: { title: 'Privacy Policy' }
  },
  { 
    path: 'terms-of-service', 
    component: HomeComponent,
    data: { title: 'Terms of Service' }
  },
  { 
    path: '**', 
    redirectTo: '',
    pathMatch: 'full'
  }
];

export const appConfig: ApplicationConfig = {
  providers: [
    importProvidersFrom(BrowserModule),
    importProvidersFrom(BrowserAnimationsModule),
    importProvidersFrom(HttpClientModule),
    provideHttpClient(withFetch()),
    provideHttpClient(),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    provideRouter(
      routes,
      withComponentInputBinding(),
      withRouterConfig({
        onSameUrlNavigation: 'reload',
        paramsInheritanceStrategy: 'always'
      })
    ),
    provideStore({ auth: authReducer }),
    provideEffects([AuthEffects]),
    provideStoreDevtools({
      maxAge: 25,
      logOnly: !isDevMode(),
      autoPause: true,
      trace: false,
      traceLimit: 75
    })
  ]
};
