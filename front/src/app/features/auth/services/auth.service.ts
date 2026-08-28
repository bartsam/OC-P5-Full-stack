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

  private readonly token = signal<string | null>(
    typeof localStorage !== 'undefined' ? localStorage.getItem('auth_token') : null,
  );

  readonly isLoggedIn = computed(() => !!this.token());

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.httpClient.post<AuthResponse>(`${this.apiUrl}/auth/register`, request).pipe(
      tap(response => {
        this.token.set(response.token);
        localStorage.setItem('auth_token', response.token);
      }),
      catchError(this.handleError),
    );
  }

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

  logout(): void {
    this.token.set(null);
    localStorage.removeItem('auth_token');
  }

  private handleError(error: HttpErrorResponse) {
    const message = error.error?.message ?? 'An internal error has occurred';
    return throwError(() => new Error(message));
  }
}
