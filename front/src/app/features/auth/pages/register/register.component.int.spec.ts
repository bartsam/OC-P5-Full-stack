import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component, DebugElement } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideRouter, Router } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { environment } from '../../../../../environments/environment';
import { NotificationService } from '../../../../shared/services/notification.service';
import { RegisterComponent } from './register.component';

@Component({ template: '' })
class DummyComponent {}

describe('RegisterComponent integration tests', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let debugElement: DebugElement;
  let httpMock: HttpTestingController;
  let router: Router;
  let mockNotificationService: { error: ReturnType<typeof vi.fn> };

  const apiUrl = `${environment.apiUrl}/auth/register`;
  const registerRequest = {
    email: 'jean.biche@example.com',
    username: 'jeanbiche',
    password: 'Password123!',
  };

  beforeEach(async () => {
    localStorage.clear();
    mockNotificationService = {
      error: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: '', component: DummyComponent }]),
        { provide: NotificationService, useValue: mockNotificationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should register successfully, persist the token, and navigate home', async () => {
    const navigateSpy = vi.spyOn(router, 'navigate');

    component.form.setValue(registerRequest);
    fixture.detectChanges();

    const submitButton = debugElement.query(By.css('[data-testid="submit-button"]'));
    expect(submitButton).toBeTruthy();
    submitButton!.nativeElement.click();

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(registerRequest);
    req.flush({ token: 'fake.jwt.token' });
    await fixture.whenStable();

    expect(navigateSpy).toHaveBeenCalledWith(['/']);
    expect(localStorage.getItem('auth_token')).toBe('fake.jwt.token');
    expect(mockNotificationService.error).not.toHaveBeenCalled();
  });

  it('should display an error notification when registration is rejected', async () => {
    component.form.setValue(registerRequest);
    fixture.detectChanges();

    const submitButton = debugElement.query(By.css('[data-testid="submit-button"]'));
    expect(submitButton).toBeTruthy();
    submitButton!.nativeElement.click();

    const req = httpMock.expectOne(apiUrl);
    req.flush({ message: 'Email already exists' }, { status: 409, statusText: 'Conflict' });
    await fixture.whenStable();

    expect(mockNotificationService.error).toHaveBeenCalledWith(
      "Impossible de s'enregistrer : Email already exists",
    );
    expect(localStorage.getItem('auth_token')).toBeNull();
  });
});
