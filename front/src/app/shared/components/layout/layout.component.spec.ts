import { Component, DebugElement } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { Router, provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthService } from '../../../features/auth/services/auth.service';
import { LayoutComponent } from './layout.component';

@Component({ template: '' })
class DummyComponent {}

describe('LayoutComponent Unit tests', () => {
  let component: LayoutComponent;
  let fixture: ComponentFixture<LayoutComponent>;
  let debugElement: DebugElement;
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
          { path: 'login', component: DummyComponent },
        ]),
        { provide: AuthService, useValue: mockAuthService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LayoutComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    fixture.destroy();
    document.body.style.removeProperty('overflow');
    vi.restoreAllMocks();
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

      const header = debugElement.query(By.css('header'));
      expect(header).toBeNull();
    });

    it('should show header on home page when user IS logged in', async () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      await router.navigateByUrl('/');
      fixture.detectChanges();

      expect(component.hideHeader()).toBeFalsy();

      const header = debugElement.query(By.css('header'));
      expect(header).toBeTruthy();
    });

    it('should show header on other pages even if user is NOT logged in', async () => {
      mockAuthService.isLoggedIn.mockReturnValue(false);
      await router.navigateByUrl('/dummy-route');
      fixture.detectChanges();

      expect(component.isHomePage()).toBeFalsy();
      expect(component.hideHeader()).toBeFalsy();

      const header = debugElement.query(By.css('header'));
      expect(header).toBeTruthy();
    });
  });

  describe('Header interactions', () => {
    it('should hide navigation links when logged out on a non-home page', async () => {
      mockAuthService.isLoggedIn.mockReturnValue(false);
      await router.navigateByUrl('/dummy-route');
      fixture.detectChanges();

      const header = debugElement.query(By.css('header'));
      const nav = debugElement.query(By.css('.nav'));

      expect(header).toBeTruthy();
      expect(nav).toBeNull();
    });

    it('should open the mobile menu and lock document scrolling', () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      fixture.detectChanges();

      const burgerButton = debugElement.query(By.css('[data-testid="burger-button"]'));
      expect(burgerButton).toBeTruthy();

      burgerButton.nativeElement.click();
      fixture.detectChanges();

      expect(component.isMenuOpen()).toBe(true);

      const nav = debugElement.query(By.css('.nav'));
      expect(nav).toBeTruthy();
      expect(nav.nativeElement.classList.contains('nav--open')).toBe(true);
      expect(document.body.style.overflow).toBe('hidden');

      const backdropButton = debugElement.query(By.css('[data-testid="backdrop-button"]'));
      expect(backdropButton).toBeTruthy();
    });

    it('should close the mobile menu and restore document scrolling from the backdrop', () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      fixture.detectChanges();

      const burgerButton = debugElement.query(By.css('[data-testid="burger-button"]'));
      burgerButton.nativeElement.click();
      fixture.detectChanges();

      const backdropButton = debugElement.query(By.css('[data-testid="backdrop-button"]'));
      backdropButton.nativeElement.click();
      fixture.detectChanges();

      expect(component.isMenuOpen()).toBe(false);
      expect(document.body.style.overflow).toBe('');
    });

    it('should close the mobile menu when clicking a nav link', async () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      fixture.detectChanges();

      const burgerButton = debugElement.query(By.css('[data-testid="burger-button"]'));
      burgerButton.nativeElement.click();
      fixture.detectChanges();

      const navLink = debugElement.query(By.css('.nav__link'));
      navLink.nativeElement.click();
      fixture.detectChanges();

      expect(component.isMenuOpen()).toBe(false);
    });

    it('should call authService.logout and navigate home when logout button is clicked', () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      fixture.detectChanges();

      const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

      const logoutButton = debugElement.query(By.css('[data-testid="logout-button"]'));
      expect(logoutButton).toBeTruthy();

      logoutButton.nativeElement.click();

      expect(mockAuthService.logout).toHaveBeenCalledTimes(1);
      expect(navigateSpy).toHaveBeenCalledWith(['/']);
    });
  });

  describe('Back button', () => {
    it('should show the back button on the login page', async () => {
      mockAuthService.isLoggedIn.mockReturnValue(false);
      await router.navigateByUrl('/login');
      fixture.detectChanges();

      const backButton = debugElement.query(By.css('[data-testid="back-button"]'));

      expect(component.showBackButton()).toBe(true);
      expect(backButton).toBeTruthy();
    });

    it('should hide the back button outside authentication pages', async () => {
      mockAuthService.isLoggedIn.mockReturnValue(false);
      await router.navigateByUrl('/dummy-route');
      fixture.detectChanges();

      const backButton = debugElement.query(By.css('[data-testid="back-button"]'));

      expect(component.showBackButton()).toBe(false);
      expect(backButton).toBeNull();
    });
  });

  describe('Accessibility', () => {
    it('should have aria-expanded attribute on burger button', () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      fixture.detectChanges();

      const burgerButton = debugElement.query(By.css('[data-testid="burger-button"]'));
      expect(burgerButton.nativeElement.getAttribute('aria-expanded')).toBe('false');

      burgerButton.nativeElement.click();
      fixture.detectChanges();

      expect(burgerButton.nativeElement.getAttribute('aria-expanded')).toBe('true');
    });

    it('should have aria-controls attribute pointing to menu when open', () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      fixture.detectChanges();

      const burgerButton = debugElement.query(By.css('[data-testid="burger-button"]'));
      expect(burgerButton.nativeElement.getAttribute('aria-controls')).toBeNull();

      burgerButton.nativeElement.click();
      fixture.detectChanges();

      expect(burgerButton.nativeElement.getAttribute('aria-controls')).toBe('menu');
    });
  });
});
