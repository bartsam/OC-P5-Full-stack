import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly httpClient = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  readonly token = signal<string | null>(
    typeof localStorage !== 'undefined' ? localStorage.getItem('auth_token') : null,
  );

  readonly isLoggedIn = computed(() => !!this.token());

  /**
   * Creates new user and authenticates it immediately. Returns a JWT token for authenticated calls.
   * @param request - The registration credentials.
   * @returns An Observable emitting the authentication response with JWT.
   */
  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.httpClient.post<AuthResponse>(`${this.apiUrl}/auth/register`, request).pipe(
      tap(response => {
        this.token.set(response.token);
        localStorage.setItem('auth_token', response.token);
      }),
      catchError(this.handleError),
    );
  }

  /**
   * Authenticates user with email/username and password. Returns a JWT token for authenticated calls.
   * @param request - The login credentials.
   * @returns An Observable emitting the authentication response with JWT.
   */
  login(request: LoginRequest): Observable<AuthResponse> {
    return this.httpClient.post<AuthResponse>(`${this.apiUrl}/auth/login`, request).pipe(
      tap(response => {
        this.token.set(response.token);
        localStorage.setItem('auth_token', response.token);
      }),
      catchError(error => {
        this.token.set(null);
        localStorage.removeItem('auth_token');
        return this.handleError(error);
      }),
    );
  }

  /**
   * Log out the user by clearing the authentication token from state and local storage.
   */
  logout(): void {
    this.token.set(null);
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem('auth_token');
    }
  }

  /**
   * Handles HTTP response errors with server error message or providing a default message.
   * @param error - The HttpErrorResponse received from the HTTP client.
   * @returns An Observable throwing a standard JavaScript Error.
   */
  private handleError(error: HttpErrorResponse) {
    const message = error.error?.message ?? 'An internal error has occurred';
    return throwError(() => new Error(message));
  }
}
