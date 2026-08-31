import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { fail } from 'assert';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { UserUpdateRequest } from '../models/update.model';
import { User } from '../models/user.model';
import { UserService } from './user.service';

describe('AuthService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;
  let apiUrl: string;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [UserService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);

    apiUrl = `${service['apiUrl']}/profile`;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getUser', () => {
    it('should call GET /api/profile and return a User', () => {
      const mockUser: User = {
        id: 1,
        email: 'john.doe@example.com',
        username: 'jeanbiche',
        createdAt: '2026-08-31T10:00:00Z',
        updatedAt: '2026-08-31T10:00:00Z',
      };

      service.getUser().subscribe(user => {
        expect(user).toEqual(mockUser);
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('GET');

      req.flush(mockUser);
    });

    it('should propagate error when getUser returns 401', () => {
      service.getUser().subscribe({
        next: () => fail('expected error'),
        error: error => {
          expect(error.status).toBe(401);
        },
      });

      const req = httpMock.expectOne(`${service['apiUrl']}/profile`);
      req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('updateUser', () => {
    it('should call PUT /api/profile with the correct body and return a User', () => {
      const updateRequest: UserUpdateRequest = {
        email: 'new.john@example.com',
        username: 'newjeanbiche',
        password: 'NewPassword123!',
      };

      const mockUser: User = {
        id: 1,
        email: updateRequest.email,
        username: updateRequest.username,
        createdAt: '2026-08-31T10:00:00Z',
        updatedAt: '2026-08-31T10:00',
      };

      service.updateUser(updateRequest).subscribe(user => {
        expect(user).toEqual(mockUser);
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updateRequest);

      req.flush(mockUser);
    });

    it('should propagate error when updateUser returns 409', () => {
      const updateRequest: UserUpdateRequest = {
        email: 'duplicate@example.com',
        username: 'duplicateUser',
        password: 'NewPassword123!',
      };

      service.updateUser(updateRequest).subscribe({
        next: () => fail('expected error'),
        error: error => {
          expect(error.status).toBe(409);
        },
      });

      const req = httpMock.expectOne(`${service['apiUrl']}/profile`);
      req.flush({ message: 'Email is already in use' }, { status: 409, statusText: 'Conflict' });
    });
  });
});
