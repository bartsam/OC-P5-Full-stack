import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { environment } from '../../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models';
import { AuthService } from './auth.service';
describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const authApiUrl = `${environment.apiUrl}/auth`;
  const mockToken = 'fake.jwt.token';

  const setup = () => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  };

  beforeEach(() => {
    localStorage.clear();
    setup();
  });

  afterEach(() => {
    httpMock.verify();
    vi.unstubAllGlobals();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('register', () => {
    it('should call /api/auth/register and store token', () => {
      const registerRequest: RegisterRequest = {
        email: 'john.doe@example.com',
        username: 'jeanbiche',
        password: 'Password123!',
      };

      service.register(registerRequest).subscribe((response: AuthResponse) => {
        expect(response.token).toBe(mockToken);
        expect(localStorage.getItem('auth_token')).toBe(mockToken);
        expect(service.isLoggedIn()).toBe(true);
      });

      const req = httpMock.expectOne(`${authApiUrl}/register`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(registerRequest);

      req.flush({ token: mockToken });
    });

    it('should handle 409 Conflict error when email already exists', () => {
      const registerRequest: RegisterRequest = {
        email: 'john.doe@example.com',
        username: 'jeanbiche',
        password: 'Password123!',
      };

      service.register(registerRequest).subscribe({
        next: () => expect.fail('The register request should have failed with a 409 error'),
        error: (error: Error) => {
          expect(error.message).toBe('Email is already in use');
          expect(localStorage.getItem('auth_token')).toBeNull();
        },
      });

      const req = httpMock.expectOne(`${authApiUrl}/register`);
      req.flush({ message: 'Email is already in use' }, { status: 409, statusText: 'Conflict' });
    });
  });

  describe('login', () => {
    it('should call /api/auth/login and store token', () => {
      const loginRequest: LoginRequest = {
        identifier: 'john.doe@example.com',
        password: 'Password123!',
      };

      service.login(loginRequest).subscribe(response => {
        expect(response.token).toBe(mockToken);
        expect(localStorage.getItem('auth_token')).toBe(mockToken);
        expect(service.isLoggedIn()).toBe(true);
      });

      const req = httpMock.expectOne(`${authApiUrl}/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(loginRequest);

      req.flush({ token: mockToken });
    });

    it('should handle 401 Unauthorized error on login failure', () => {
      const loginRequest: LoginRequest = {
        identifier: 'john.doe@example.com',
        password: 'wrongPassword123!',
      };

      service.login(loginRequest).subscribe({
        next: () => expect.fail('The login request should have failed with a 401 error'),
        error: (error: Error) => {
          expect(error.message).toBe('Bad credentials');
          expect(localStorage.getItem('auth_token')).toBeNull();
          expect(service.isLoggedIn()).toBe(false);
        },
      });

      const req = httpMock.expectOne(`${authApiUrl}/login`);
      req.flush({ message: 'Bad credentials' }, { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('logout', () => {
    it('should clear token from localStorage and state', () => {
      const loginRequest: LoginRequest = {
        identifier: 'john.doe@example.com',
        password: 'Password123!',
      };

      service.login(loginRequest).subscribe();

      const req = httpMock.expectOne(`${authApiUrl}/login`);
      req.flush({ token: mockToken });

      service.logout();

      expect(localStorage.getItem('auth_token')).toBeNull();
      expect(service.isLoggedIn()).toBe(false);
    });
  });

  describe('localStorage', () => {
    it('should restore the authentication state from localStorage', () => {
      localStorage.setItem('auth_token', mockToken);
      setup();
      expect(service.isLoggedIn()).toBe(true);
    });

    it('should initialise as logged out when localStorage is unavailable', () => {
      vi.stubGlobal('localStorage', undefined);
      setup();
      expect(service.isLoggedIn()).toBe(false);
    });
  });
});
