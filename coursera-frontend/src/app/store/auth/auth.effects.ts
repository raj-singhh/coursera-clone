import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import * as AuthActions from './auth.actions';
import { AuthService } from '../../auth.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthEffects {
  private readonly actions$ = inject(Actions);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  login$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(AuthActions.login),
      tap(action => console.log('Login action dispatched:', action)),
      switchMap((action) =>
        this.authService.login({ username: action.username, password: action.password }).pipe(
          tap(response => console.log('Login response:', response)),
          map((response) => AuthActions.loginSuccess({ token: response.token })),
          catchError((error) => {
            console.error('Login error:', error);
            return of(AuthActions.loginFailure({ error }));
          })
        )
      )
    );
  });

  loginSuccess$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(AuthActions.loginSuccess),
      tap(() => this.router.navigate(['/']))
    );
  }, { dispatch: false });

  logout$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(AuthActions.logout),
      tap(() => {
        this.authService.logout();
        this.router.navigate(['/login']);
      })
    );
  }, { dispatch: false });

  constructor() {}
}
