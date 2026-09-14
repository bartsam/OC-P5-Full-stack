import { Component, DebugElement, signal, WritableSignal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideRouter, Router } from '@angular/router';
import { beforeEach, describe, expect, it } from 'vitest';

import { AuthService } from '../../features/auth/services/auth.service';
import { HomeComponent } from './home.component';

@Component({ template: '' })
class DummyComponent {}

describe('HomeComponent', () => {
  let fixture: ComponentFixture<HomeComponent>;
  let component: HomeComponent;
  let debugElement: DebugElement;
  let router: Router;

  let isLoggedIn: WritableSignal<boolean>;
  let mockAuthService: { isLoggedIn: WritableSignal<boolean> };

  beforeEach(async () => {
    isLoggedIn = signal(false);
    mockAuthService = { isLoggedIn };

    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [
        provideRouter([
          { path: 'login', component: DummyComponent },
          { path: 'register', component: DummyComponent },
          { path: 'posts/feed', component: DummyComponent },
        ]),
        { provide: AuthService, useValue: mockAuthService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
    router = TestBed.inject(Router);
  });

  it('should create', () => {
    isLoggedIn.set(false);
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  describe('when logged out', () => {
    beforeEach(() => {
      isLoggedIn.set(false);
      fixture.detectChanges();
    });

    it('should not navigate away', () => {
      expect(router.url).toBe('/');
    });

    it('should display login and register links', () => {
      const links = debugElement.queryAll(By.css('[data-testid="link"]'));

      expect(links).toHaveLength(2);
      expect(links[0].nativeElement.getAttribute('href')).toBe('/login');
      expect(links[1].nativeElement.getAttribute('href')).toBe('/register');
    });
  });

  describe('when logged in', () => {
    beforeEach(async () => {
      isLoggedIn.set(true);
      fixture.detectChanges();
      await fixture.whenStable();
    });

    it('should navigate to /posts/feed', () => {
      expect(router.url).toBe('/posts/feed');
    });

    it('should display the loading screen instead of the landing actions', () => {
      const landing = debugElement.query(By.css('[data-testid="landing"]'));
      const loading = debugElement.query(By.css('[data-testid="loading-screen"]'));

      expect(loading).toBeTruthy();
      expect(landing).toBeFalsy();
    });
  });
});
