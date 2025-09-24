import { createReducer, on } from '@ngrx/store';
import * as AuthActions from './auth.actions';

export interface AuthState {
  token: string | null;
  error: any | null;
  loading: boolean;
}

export const initialState: AuthState = {
  token: null,
  error: null,
  loading: false,
};

export const authReducer = createReducer(
  initialState,
  on(AuthActions.login, (state) => ({ ...state, loading: true, error: null })),
  on(AuthActions.loginSuccess, (state, { token }) => ({ ...state, token, loading: false, error: null })),
  on(AuthActions.loginFailure, (state, { error }) => ({ ...state, error, loading: false })),
  on(AuthActions.logout, (state) => ({ ...state, token: null, error: null, loading: false }))
);
