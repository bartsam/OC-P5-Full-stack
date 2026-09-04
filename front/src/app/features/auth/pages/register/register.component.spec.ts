import { HttpErrorResponse } from '@angular/common/http';
import { DebugElement } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { NotificationService } from '../../../../shared/services/notification.service';
import { AuthService } from '../../services/auth.service';
import { RegisterComponent } from './register.component';

describe('RegisterComponent unit tests', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let debugElement: DebugElement;
  let mockAuthService: { register: ReturnType<typeof vi.fn> };
  let mockNotificationService: { error: ReturnType<typeof vi.fn> };
  let router: Router;

  beforeEach(async () => {
    mockAuthService = {
      register: vi.fn(),
    };
    mockNotificationService = {
      error: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [RegisterComponent, ReactiveFormsModule],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: mockAuthService },
        { provide: NotificationService, useValue: mockNotificationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create with default invalid form', () => {
    expect(component).toBeTruthy();
    expect(component.form.valid).toBeFalsy();
    expect(component.form.controls.username.valid).toBeFalsy();
    expect(component.form.controls.email.valid).toBeFalsy();
    expect(component.form.controls.password.valid).toBeFalsy();
  });

  describe('Username field', () => {
    it('should be invalid if empty', () => {
      const control = component.form.controls.username;
      control.setValue('');
      expect(control.hasError('required')).toBeTruthy();
    });

    it('should be invalid if it contains fewer than 3 characters', () => {
      const control = component.form.controls.username;
      control.setValue('ab');
      expect(control.hasError('minlength')).toBeTruthy();
    });

    it('should be invalid if it contains more than 20 characters', () => {
      const control = component.form.controls.username;
      control.setValue('a'.repeat(21));
      expect(control.hasError('maxlength')).toBeTruthy();
    });

    it('should be valid with a correct username', () => {
      const control = component.form.controls.username;
      control.setValue('jeanbiche');
      expect(control.valid).toBeTruthy();
    });
  });

  describe('Email field', () => {
    it('should be invalid if empty', () => {
      const control = component.form.controls.email;
      control.setValue('');
      expect(control.hasError('required')).toBeTruthy();
    });

    it('should be invalid if the email format is incorrect', () => {
      const control = component.form.controls.email;
      control.setValue('invalid-email');
      expect(control.hasError('email')).toBeTruthy();
    });

    it('should be invalid if it contains more than 50 characters', () => {
      const control = component.form.controls.email;
      control.setValue(`${'a'.repeat(50)}@example.com`);
      expect(control.hasError('maxlength')).toBeTruthy();
    });

    it('should be valid with a correct email address', () => {
      const control = component.form.controls.email;
      control.setValue('john.doe@example.com');
      expect(control.valid).toBeTruthy();
    });
  });

  describe('Password field', () => {
    it('should be invalid if empty', () => {
      const control = component.form.controls.password;
      control.setValue('');
      expect(control.hasError('required')).toBeTruthy();
    });

    it('should be invalid if the pattern is not followed', () => {
      const control = component.form.controls.password;

      control.setValue('password123');
      expect(control.hasError('pattern')).toBeTruthy();

      control.setValue('P@ss1');
      expect(control.hasError('pattern')).toBeTruthy();
    });

    it('should toggle the password visibility', () => {
      expect(component.isPasswordVisible()).toBeFalsy();

      component.form.controls.password.setValue('Password123!');
      fixture.detectChanges();

      const button = debugElement.query(By.css('[data-testid="password-button"]'));
      expect(button).toBeTruthy();

      button.nativeElement.click();
      fixture.detectChanges();

      expect(component.isPasswordVisible()).toBeTruthy();

      const input = debugElement.query(By.css('[data-testid="password-input"]'));
      expect(input.nativeElement.getAttribute('type')).toBe('text');
    });

    it('should be valid if the password meets the pattern', () => {
      const control = component.form.controls.password;
      control.setValue('Password123!');
      expect(control.valid).toBeTruthy();
    });
  });

  describe('Submit form', () => {
    it('should disable submit button if the form is invalid', () => {
      const button = debugElement.query(By.css('[data-testid="submit-button"]'));
      expect(button.nativeElement.disabled).toBeTruthy();
    });

    it('should enable submit button if the form is valid', () => {
      component.form.controls.username.setValue('jeanbiche');
      component.form.controls.email.setValue('jean.biche@example.com');
      component.form.controls.password.setValue('Password123!');
      fixture.detectChanges();

      const button = debugElement.query(By.css('[data-testid="submit-button"]'));
      expect(button.nativeElement.disabled).toBeFalsy();
    });

    it('should not call AuthService.register if the form is invalid', () => {
      component.submit();
      expect(mockAuthService.register).not.toHaveBeenCalled();
    });

    it('should call AuthService.register and redirect to “/” if successful', () => {
      const navigateSpy = vi.spyOn(router, 'navigate');
      mockAuthService.register.mockReturnValue(of({ token: 'fake.jwt.token' }));

      component.form.controls.username.setValue('jeanbiche');
      component.form.controls.email.setValue('jean.biche@example.com');
      component.form.controls.password.setValue('Password123!');

      component.submit();

      expect(mockAuthService.register).toHaveBeenCalledWith({
        username: 'jeanbiche',
        email: 'jean.biche@example.com',
        password: 'Password123!',
      });
      expect(navigateSpy).toHaveBeenCalledWith(['/']);
    });

    it('should show a notification in the event of an HTTP failure', () => {
      const mockError = new HttpErrorResponse({
        error: 'Email already exists',
        status: 409,
        statusText: 'Conflict',
      });
      Object.defineProperty(mockError, 'message', { value: 'Email already exists' });

      mockAuthService.register.mockReturnValue(throwError(() => mockError));

      component.form.controls.username.setValue('jeanbiche');
      component.form.controls.email.setValue('existing@example.com');
      component.form.controls.password.setValue('Password123!');

      component.submit();

      expect(mockNotificationService.error).toHaveBeenCalledWith(
        "Impossible de s'enregistrer : Email already exists",
      );
    });
  });
});
