import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';

import { provideHttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { User } from '../../models/user.model';
import { ProfileComponent } from './profile.component';

describe('ProfileComponent integration', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let httpMock: HttpTestingController;
  let apiUrl: string;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProfileComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    apiUrl = `${environment.apiUrl}/profile`;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should load user, prefill form and render fields on success', () => {
    const mockUser: User = {
      id: 1,
      email: 'john.doe@example.com',
      username: 'jeanbiche',
      createdAt: '2026-08-31T10:00:00Z',
      updatedAt: '2026-08-31T10:00:00Z',
    };

    fixture.detectChanges();

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');

    req.flush(mockUser);
    fixture.detectChanges();

    expect(component.isLoading()).toBe(false);
    expect(component.errorGetMessage()).toBeNull();

    expect(component.form.value.email).toBe('john.doe@example.com');
    expect(component.form.value.username).toBe('jeanbiche');

    const usernameInput = fixture.nativeElement.querySelector('[data-testid="username-input"]');
    const emailInput = fixture.nativeElement.querySelector('[data-testid="email-input"]');
    const submitButton = fixture.nativeElement.querySelector('[data-testid="submit-button"]');

    expect(usernameInput).toBeTruthy();
    expect(emailInput).toBeTruthy();
    expect(submitButton).toBeTruthy();
  });

  it('should display error message when getUser fails', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');

    req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
    fixture.detectChanges();

    expect(component.isLoading()).toBe(false);
    expect(component.errorGetMessage()).toBe('Impossible de charger le profil.');

    const errorElement = fixture.nativeElement.querySelector('[data-testid="error-get-message"]');
    expect(errorElement).toBeTruthy();
    expect(errorElement.textContent).toContain('Impossible de charger le profil.');
  });

  it('should call updateUser on submit and handle success', () => {
    const mockUser: User = {
      id: 1,
      email: 'john.doe@example.com',
      username: 'jeanbiche',
      createdAt: '2026-08-31T10:00:00Z',
      updatedAt: '2026-08-31T10:00:00Z',
    };

    fixture.detectChanges();

    const reqGet = httpMock.expectOne(apiUrl);
    expect(reqGet.request.method).toBe('GET');
    reqGet.flush(mockUser);
    fixture.detectChanges();

    component.form.setValue({
      email: 'new.john@example.com',
      username: 'newjeanbiche',
      password: 'NewPassword123!',
    });

    fixture.detectChanges();

    const submitButton = fixture.nativeElement.querySelector('[data-testid="submit-button"]');
    submitButton.click();

    const reqPut = httpMock.expectOne(apiUrl);
    expect(reqPut.request.method).toBe('PUT');
    expect(reqPut.request.body).toEqual({
      email: 'new.john@example.com',
      username: 'newjeanbiche',
      password: 'NewPassword123!',
    });

    reqPut.flush({});
    fixture.detectChanges();

    expect(component.isLoading()).toBe(false);
    expect(component.errorPutMessage()).toBeNull();
  });

  it('should display error message when updateUser fails', () => {
    const mockUser: User = {
      id: 1,
      email: 'john.doe@example.com',
      username: 'jeanbiche',
      createdAt: '2026-08-31T10:00:00Z',
      updatedAt: '2026-08-31T10:00:00Z',
    };

    fixture.detectChanges();

    const reqGet = httpMock.expectOne(apiUrl);
    reqGet.flush(mockUser);
    fixture.detectChanges();

    component.form.setValue({
      email: 'new.john@example.com',
      username: 'newjeanbiche',
      password: 'NewPassword123!',
    });

    fixture.detectChanges();

    const submitButton = fixture.nativeElement.querySelector('[data-testid="submit-button"]');
    submitButton.click();

    const reqPut = httpMock.expectOne(apiUrl);
    reqPut.flush({ message: 'Conflict' }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();

    expect(component.isLoading()).toBe(false);

    const errorElement = fixture.nativeElement.querySelector('[data-testid="error-put-message"]');
    expect(errorElement).toBeTruthy();
    expect(errorElement.textContent).toContain('Impossible de modifier le profil.');
  });
});
