import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthService } from '../../../features/auth/services/auth.service';
import { LayoutComponent } from './layout.component';

@Component({ template: '' })
class DummyComponent {}

describe('LayoutComponent Unit tests', () => {
  let component: LayoutComponent;
  let fixture: ComponentFixture<LayoutComponent>;
  let mockAuthService: { isLoggedIn: ReturnType<typeof vi.fn>; logout: ReturnType<typeof vi.fn> };
  let router: Router;

  beforeEach(async () => {
    mockAuthService = {
      isLoggedIn: vi.fn(),
      logout: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [LayoutComponent],
      providers: [
        provideRouter([
          { path: '', component: DummyComponent },
          { path: 'dummy-route', component: DummyComponent },
        ]),
        { provide: AuthService, useValue: mockAuthService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LayoutComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Header display', () => {
    it('should hide header on home page when user is NOT logged in', async () => {
      mockAuthService.isLoggedIn.mockReturnValue(false);
      await router.navigateByUrl('/');
      fixture.detectChanges();

      expect(component.hideHeader()).toBeTruthy();

      const header = fixture.nativeElement.querySelector('header');
      expect(header).toBeNull();
    });

    it('should show header on home page when user IS logged in', async () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      await router.navigateByUrl('/');
      fixture.detectChanges();

      expect(component.hideHeader()).toBeFalsy();

      const header = fixture.nativeElement.querySelector('header');
      expect(header).toBeTruthy();
    });

    it('should show header on other pages even if user is NOT logged in', async () => {
      mockAuthService.isLoggedIn.mockReturnValue(false);
      await router.navigateByUrl('/dummy-route');
      fixture.detectChanges();

      expect(component.isHomePage()).toBeFalsy();
      expect(component.hideHeader()).toBeFalsy();

      const headerEl = fixture.nativeElement.querySelector('header');
      expect(headerEl).toBeTruthy();
    });
  });

  describe('Header interactions', () => {
    it('should show navigation links only when logged in', () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      fixture.detectChanges();

      const navEl = fixture.nativeElement.querySelector('nav');
      expect(navEl).toBeTruthy();
    });

    it('should hide navigation links when logged out on a non-home page', async () => {
      mockAuthService.isLoggedIn.mockReturnValue(false);
      await router.navigateByUrl('/dummy-route');
      fixture.detectChanges();

      const header = fixture.nativeElement.querySelector('header');
      const nav = fixture.nativeElement.querySelector('nav');

      expect(header).toBeTruthy();
      expect(nav).toBeNull();
    });

    it('should call authService.logout when logout button is clicked', () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      fixture.detectChanges();

      const logoutButton = fixture.nativeElement.querySelector('[data-testid="logout-button"]');
      expect(logoutButton).toBeTruthy();

      logoutButton.click();
      expect(mockAuthService.logout).toHaveBeenCalledTimes(1);
    });
  });
});
