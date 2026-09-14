import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { AuthService } from '../../../features/auth/services/auth.service';
import { HeaderComponent } from './header.component';

describe('HeaderComponent', () => {
  let fixture: ComponentFixture<HeaderComponent>;
  let component: HeaderComponent;
  let isLoggedIn: ReturnType<typeof signal<boolean>>;
  let authServiceMock: {
    isLoggedIn: ReturnType<typeof signal<boolean>>;
    logout: ReturnType<typeof vi.fn>;
  };
  let router: Router;

  beforeEach(async () => {
    isLoggedIn = signal(false);
    authServiceMock = {
      isLoggedIn,
      logout: vi.fn(),
    };
    await TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
    document.body.style.removeProperty('overflow');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should start with the menu closed', () => {
    expect(component.isMenuOpen()).toBe(false);
  });

  it('toggleMenu should toggle isMenuOpen', () => {
    component.toggleMenu();
    expect(component.isMenuOpen()).toBe(true);

    component.toggleMenu();
    expect(component.isMenuOpen()).toBe(false);
  });

  it('closeMenu should set isMenuOpen to false', () => {
    component.toggleMenu();
    expect(component.isMenuOpen()).toBe(true);

    component.closeMenu();
    expect(component.isMenuOpen()).toBe(false);
  });

  it('logout should call authService.logout and navigate to /', () => {
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.logout();

    expect(authServiceMock.logout).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/']);
  });

  it('should lock body scroll when menu opens and restore it when it closes', () => {
    component.toggleMenu();
    fixture.detectChanges();
    expect(document.body.style.overflow).toBe('hidden');

    component.closeMenu();
    fixture.detectChanges();
    expect(document.body.style.overflow).toBe('');
  });

  it('should not render nav or burger button when logged out', () => {
    const nav = fixture.nativeElement.querySelector('[data-testid="nav"]');
    const burger = fixture.nativeElement.querySelector('[data-testid="burger-button"]');

    expect(nav).toBeNull();
    expect(burger).toBeNull();
  });

  describe('when logged in', () => {
    beforeEach(() => {
      isLoggedIn.set(true);
      fixture.detectChanges();
    });

    it('should render the nav and burger button', () => {
      const nav = fixture.nativeElement.querySelector('[data-testid="nav"]');
      const burger = fixture.nativeElement.querySelector('[data-testid="burger-button"]');

      expect(nav).toBeTruthy();
      expect(burger).toBeTruthy();
    });

    it('clicking the burger button should call toggleMenu', () => {
      const toggleSpy = vi.spyOn(component, 'toggleMenu');
      const burger = fixture.nativeElement.querySelector('[data-testid="burger-button"]');

      burger.click();

      expect(toggleSpy).toHaveBeenCalled();
    });

    it('clicking the logout button should call logout and closeMenu', () => {
      const logoutSpy = vi.spyOn(component, 'logout');
      const closeMenuSpy = vi.spyOn(component, 'closeMenu');
      const logoutButton = fixture.nativeElement.querySelector('[data-testid="logout-button"]');

      logoutButton.click();

      expect(logoutSpy).toHaveBeenCalled();
      expect(closeMenuSpy).toHaveBeenCalled();
    });

    it('should show the backdrop only when the menu is open', () => {
      let backdrop = fixture.nativeElement.querySelector('[data-testid="backdrop-button"]');
      expect(backdrop).toBeNull();

      component.toggleMenu();
      fixture.detectChanges();

      backdrop = fixture.nativeElement.querySelector('[data-testid="backdrop-button"]');
      expect(backdrop).toBeTruthy();
    });

    it('clicking the backdrop should call closeMenu', () => {
      const closeMenuSpy = vi.spyOn(component, 'closeMenu');
      component.toggleMenu();
      fixture.detectChanges();

      const backdrop = fixture.nativeElement.querySelector('[data-testid="backdrop-button"]');
      backdrop.click();

      expect(closeMenuSpy).toHaveBeenCalled();
    });
  });
});
