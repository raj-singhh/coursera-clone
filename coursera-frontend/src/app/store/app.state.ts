import { AuthState } from './auth/auth.reducer';

export interface AppState {
  auth: AuthState;
  // Define other application states here
  // courses: CoursesState;
}
