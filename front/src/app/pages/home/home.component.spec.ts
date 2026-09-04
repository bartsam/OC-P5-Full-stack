import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthService } from '../../features/auth/services/auth.service';
import { HomeComponent } from './home.component';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let debugElement: DebugElement;
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
    debugElement = fixture.debugElement;
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
      const landing = debugElement.query(By.css('[data-testid="landing"]'));
      const feed = debugElement.query(By.css('[data-testid="feed"]'));
      const links = debugElement.queryAll(By.css('[data-testid="link"]'));

      expect(landing).not.toBeNull();
      expect(feed).toBeNull();

      expect(links.length).toBe(2);
      expect(links[0].nativeElement.getAttribute('href')).toBe('/login');
      expect(links[1].nativeElement.getAttribute('href')).toBe('/register');
    });
  });

  describe('when the user is logged in', () => {
    beforeEach(() => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      fixture.detectChanges();
    });

    it('should display the feed of posts', () => {
      const landing = debugElement.query(By.css('[data-testid="landing"]'));
      const feed = debugElement.query(By.css('[data-testid="feed"]'));

      expect(feed).not.toBeNull();
      expect(landing).toBeNull();
    });
  });
});
