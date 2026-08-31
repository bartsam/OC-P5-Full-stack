import { ComponentFixture, TestBed } from '@angular/core/testing';

import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthService } from '../../features/auth/services/auth.service';
import { HomeComponent } from './home.component';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let mockAuthService: { isLoggedIn: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    mockAuthService = {
      isLoggedIn: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [{ provide: AuthService, useValue: mockAuthService }, provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('when the user is not logged in', () => {
    beforeEach(() => {
      mockAuthService.isLoggedIn.mockReturnValue(false);
      fixture.detectChanges();
    });

    it('should display the landing section with the sign-in/sign-up links', () => {
      const landing = fixture.nativeElement.querySelector('[data-testid="landing"]');
      const feed = fixture.nativeElement.querySelector('[data-testid="feed"]');
      const links = fixture.nativeElement.querySelectorAll('[data-testid="landing-link"]');

      expect(landing).not.toBeNull();
      expect(feed).toBeNull();

      expect(links.length).toBe(2);
      expect(links[0].getAttribute('href')).toBe('/login');
      expect(links[1].getAttribute('href')).toBe('/register');
    });
  });

  describe('when the user is logged in', () => {
    beforeEach(() => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      fixture.detectChanges();
    });

    it('should display the feed of posts', () => {
      const landing = fixture.nativeElement.querySelector('[data-testid="landing"]');
      const feed = fixture.nativeElement.querySelector('[data-testid="feed"]');

      expect(feed).not.toBeNull();
      expect(landing).toBeNull();
    });
  });
});
