import { Component, DebugElement, signal, WritableSignal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { AuthService } from '../../../features/auth/services/auth.service';
import { LayoutComponent } from './layout.component';

@Component({ template: '' })
class DummyComponent {}

describe('LayoutComponent', () => {
  let component: LayoutComponent;
  let fixture: ComponentFixture<LayoutComponent>;
  let debugElement: DebugElement;
  let router: Router;

  let isLoggedIn: WritableSignal<boolean>;
  let mockAuthService: {
    isLoggedIn: WritableSignal<boolean>;
    logout: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    isLoggedIn = signal(false);
    mockAuthService = {
      isLoggedIn,
      logout: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [LayoutComponent],
      providers: [
        provideRouter([
          { path: '', component: DummyComponent },
          { path: 'dummy-route', component: DummyComponent },
          {
            path: 'dummy-flagged-route',
            component: DummyComponent,
            data: { showBackButton: true },
          },
        ]),
        { provide: AuthService, useValue: mockAuthService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LayoutComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize currentUrl with the router initial url', () => {
    expect(component.currentUrl()).toBe(router.url);
  });

  it('should update currentUrl after a navigation', async () => {
    await router.navigateByUrl('/dummy-route');
    fixture.detectChanges();

    expect(component.currentUrl()).toBe('/dummy-route');
  });

  describe('Header', () => {
    it('should hide the header on the root route when logged out', async () => {
      isLoggedIn.set(false);
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.hideHeader()).toBe(true);
    });

    it('should show the header on the root route when logged in', async () => {
      isLoggedIn.set(true);
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.hideHeader()).toBe(false);
    });

    it('should show the header when navigating away from the root route', async () => {
      isLoggedIn.set(false);
      fixture.detectChanges();
      await fixture.whenStable();

      await router.navigateByUrl('/dummy-route');
      fixture.detectChanges();

      expect(component.hideHeader()).toBe(false);
    });

    it('should not render app-header when hideHeader is true', async () => {
      isLoggedIn.set(false);
      fixture.detectChanges();
      await fixture.whenStable();

      const header = debugElement.query(el => el.name === 'app-header');
      expect(header).toBeFalsy();
    });

    it('should render app-header when hideHeader is false', async () => {
      isLoggedIn.set(true);
      fixture.detectChanges();
      await fixture.whenStable();

      const header = debugElement.query(el => el.name === 'app-header');
      expect(header).toBeTruthy();
    });
  });

  describe('Back button', () => {
    it('should not show the back button on a route without the flag', async () => {
      await router.navigateByUrl('/dummy-route');
      fixture.detectChanges();

      expect(component.showBackButton()).toBe(false);
    });

    it('should show the back button on a route flagged with showBackButton', async () => {
      let backButton = debugElement.nativeElement.querySelector('[data-testid="back-button"]');
      expect(backButton).toBeNull();

      await router.navigateByUrl('/dummy-flagged-route');
      fixture.detectChanges();

      backButton = debugElement.nativeElement.querySelector('[data-testid="back-button"]');
      expect(backButton).toBeTruthy();
    });

    it('clicking the back button should call goBack', async () => {
      const goBackSpy = vi.spyOn(component, 'goBack');

      await router.navigateByUrl('/dummy-flagged-route');
      fixture.detectChanges();

      const backButton = debugElement.nativeElement.querySelector('[data-testid="back-button"]');
      backButton.click();

      expect(goBackSpy).toHaveBeenCalled();
    });
  });
});
