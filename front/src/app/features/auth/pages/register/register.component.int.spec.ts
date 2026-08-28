import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideRouter, Router } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../../../../environments/environment';
import { AppComponent } from '../../../../app.component';
import { routes } from '../../../../app.routes';
import { RegisterComponent } from './register.component';

describe('RegisterComponent integration tests', () => {
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

  it('should register, persist the token, redirect home, and display the authenticated UI', async () => {
    await router.navigateByUrl('/register');
    fixture.detectChanges();

    const registerComponent = fixture.debugElement.query(By.directive(RegisterComponent))
      .componentInstance as RegisterComponent;

    registerComponent.form.setValue({
      email: 'jean.biche@example.com',
      username: 'jeanbiche',
      password: 'Password123!',
    });

    registerComponent.submit();

    const request = httpMock.expectOne(`${environment.apiUrl}/auth/register`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      email: 'jean.biche@example.com',
      username: 'jeanbiche',
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
