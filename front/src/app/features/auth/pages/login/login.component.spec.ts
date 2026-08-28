import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthService } from '../../services/auth.service';
import { LoginComponent } from './login.component';

describe('LoginComponent Unit tests', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let mockAuthService: { login: ReturnType<typeof vi.fn> };
  let router: Router;

  beforeEach(async () => {
    mockAuthService = {
      login: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule],
      providers: [provideRouter([]), { provide: AuthService, useValue: mockAuthService }],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create with default invalid form', () => {
    expect(component).toBeTruthy();
    expect(component.form.valid).toBeFalsy();
    expect(component.form.controls.identifier.valid).toBeFalsy();
    expect(component.form.controls.password.valid).toBeFalsy();
    expect(component.onError).toBeFalsy();
    expect(component.errorMessage).toBeNull();
  });

  describe('Identifier field', () => {
    it('should be invalid if empty', () => {
      const control = component.form.controls.identifier;
      control.setValue('');
      expect(control.hasError('required')).toBeTruthy();
    });

    it('should be invalid if it contains fewer than 3 characters', () => {
      const control = component.form.controls.identifier;
      control.setValue('ab');
      expect(control.hasError('minlength')).toBeTruthy();
    });

    it('should be invalid if it contains more than 50 characters', () => {
      const control = component.form.controls.identifier;
      control.setValue('a'.repeat(51));
      expect(control.hasError('maxlength')).toBeTruthy();
    });

    it('should be valid with a correct username', () => {
      const control = component.form.controls.identifier;
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
      expect(component.isPasswordVisible).toBeFalsy();

      component.form.controls.password.setValue('Password123!');
      fixture.detectChanges();

      const button = fixture.nativeElement.querySelector('[data-testid="password-button"]');
      expect(button).toBeTruthy();

      button.click();
      fixture.detectChanges();

      expect(component.isPasswordVisible).toBeTruthy();

      const input = fixture.nativeElement.querySelector('[data-testid="password-input"]');
      expect(input.getAttribute('type')).toBe('text');
    });

    it('should be valid if the password meets the pattern', () => {
      const control = component.form.controls.password;
      control.setValue('Password123!');
      expect(control.valid).toBeTruthy();
    });
  });

  describe('Submit form', () => {
    it('should disable submit button if the form is invalid', () => {
      const submitBtn = fixture.nativeElement.querySelector('[data-testid="submit-button"]');
      expect(submitBtn.disabled).toBeTruthy();
    });

    it('should enable submit button if the form is valid', () => {
      component.form.controls.identifier.setValue('jeanbiche');
      component.form.controls.password.setValue('Password123!');
      fixture.detectChanges();

      const submitBtn = fixture.nativeElement.querySelector('[data-testid="submit-button"]');
      expect(submitBtn.disabled).toBeFalsy();
    });

    it('should not call AuthService.login if the form is invalid', () => {
      component.submit();
      expect(mockAuthService.login).not.toHaveBeenCalled();
    });

    it('should call AuthService.login and redirect to “/” if successful', () => {
      const navigateSpy = vi.spyOn(router, 'navigate');
      mockAuthService.login.mockReturnValue(of({ token: 'fake-jwt' }));

      component.form.controls.identifier.setValue('jeanbiche');
      component.form.controls.password.setValue('Password123!');

      component.submit();

      expect(mockAuthService.login).toHaveBeenCalledWith({
        identifier: 'jeanbiche',
        password: 'Password123!',
      });
      expect(navigateSpy).toHaveBeenCalledWith(['/']);
      expect(component.onError).toBeFalsy();
      expect(component.errorMessage).toBeNull();
    });

    it('should display an error message in the event of an HTTP failure', () => {
      const mockError = new HttpErrorResponse({
        error: 'Bad credentials',
        status: 401,
        statusText: 'Unauthorized',
      });
      Object.defineProperty(mockError, 'message', { value: 'Bad credentials' });

      mockAuthService.login.mockReturnValue(throwError(() => mockError));

      component.form.controls.identifier.setValue('unknown');
      component.form.controls.password.setValue('Password123!');

      component.submit();
      fixture.detectChanges();

      expect(component.onError).toBeTruthy();
      expect(component.errorMessage).toBe('Bad credentials');

      const errorEl = fixture.nativeElement.querySelector('[data-testid="error-message"]');
      expect(errorEl).toBeTruthy();
      expect(errorEl.textContent).toContain('Bad credentials');
    });
  });
});
