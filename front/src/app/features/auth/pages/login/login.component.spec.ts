import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component, DebugElement } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { provideRouter, Router } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { provideHttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { NotificationService } from '../../../../shared/services/notification.service';
import { LoginComponent } from './login.component';

@Component({ template: '' })
class DummyComponent {}

describe('LoginComponent integration tests', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let debugElement: DebugElement;
  let httpMock: HttpTestingController;
  let router: Router;
  let mockNotificationService: { error: ReturnType<typeof vi.fn> };

  const apiUrl = `${environment.apiUrl}/auth/login`;

  beforeEach(async () => {
    localStorage.clear();
    mockNotificationService = {
      error: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: '', component: DummyComponent }]),
        { provide: NotificationService, useValue: mockNotificationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should login successfully and navigate to home', async () => {
    const navigateSpy = vi.spyOn(router, 'navigate');

    component.form.controls.identifier.setValue('jeanbiche');
    component.form.controls.password.setValue('Password123!');
    fixture.detectChanges();

    const submitButton = debugElement.query(By.css('[data-testid="submit-button"]'));
    submitButton.nativeElement.click();

    const request = httpMock.expectOne(apiUrl);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      identifier: 'jeanbiche',
      password: 'Password123!',
    });

    request.flush({ token: 'fake.jwt.token' });

    await fixture.whenStable();

    expect(navigateSpy).toHaveBeenCalledWith(['/']);
    expect(mockNotificationService.error).not.toHaveBeenCalled();
  });

  it('should display error notification on 401', async () => {
    component.form.controls.identifier.setValue('unknown');
    component.form.controls.password.setValue('Password123!');
    fixture.detectChanges();

    const submitButton = debugElement.query(By.css('[data-testid="submit-button"]'));
    submitButton.nativeElement.click();

    const request = httpMock.expectOne(apiUrl);
    request.flush({ message: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });

    await fixture.whenStable();

    expect(mockNotificationService.error).toHaveBeenCalledWith(
      'Impossible de se connecter : Invalid credentials',
    );
  });

  it('should display error notification on 500', async () => {
    component.form.controls.identifier.setValue('jeanbiche');
    component.form.controls.password.setValue('Password123!');
    fixture.detectChanges();

    const submitButton = debugElement.query(By.css('[data-testid="submit-button"]'));
    submitButton.nativeElement.click();

    const request = httpMock.expectOne(apiUrl);
    request.flush(
      { message: 'Internal server error' },
      { status: 500, statusText: 'Internal Server Error' },
    );

    await fixture.whenStable();

    expect(mockNotificationService.error).toHaveBeenCalledWith(
      'Impossible de se connecter : Internal server error',
    );
  });
});
