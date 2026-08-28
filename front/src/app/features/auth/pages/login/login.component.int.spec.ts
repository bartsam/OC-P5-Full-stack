import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideRouter, Router } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../../../../environments/environment';
import { AppComponent } from '../../../../app.component';
import { routes } from '../../../../app.routes';
import { LoginComponent } from './login.component';

describe('Authentication integration tests', () => {
  let fixture: ComponentFixture<AppComponent>;
  let router: Router;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();

    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    router = TestBed.inject(Router);
    httpMock = TestBed.inject(HttpTestingController);

    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should log in, persist the token, redirect home, and display the authenticated UI', async () => {
    await router.navigateByUrl('/login');
    fixture.detectChanges();

    const loginComponent = fixture.debugElement.query(By.directive(LoginComponent))
      .componentInstance as LoginComponent;

    loginComponent.form.setValue({
      identifier: 'jeanbiche',
      password: 'Password123!',
    });

    loginComponent.submit();

    const request = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      identifier: 'jeanbiche',
      password: 'Password123!',
    });

    request.flush({ token: 'fake.jwt.token' });

    await fixture.whenStable();
    fixture.detectChanges();

    expect(router.url).toBe('/');
    expect(localStorage.getItem('auth_token')).toBe('fake.jwt.token');
    expect(fixture.nativeElement.querySelector('[data-testid="feed"]')).toBeTruthy();
  });
});
