import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DebugElement } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../../../../environments/environment';
import { PostItem } from '../../models';
import { FeedComponent } from './feed.component';

describe('FeedComponent integration', () => {
  let fixture: ComponentFixture<FeedComponent>;
  let debugElement: DebugElement;
  let httpMock: HttpTestingController;

  const apiUrl = `${environment.apiUrl}/posts`;

  const mockPosts: PostItem[] = [
    {
      id: 1,
      title: 'Old post',
      content: 'Old content…',
      author: 'User 1',
      createdAt: '2025-01-01T10:00:00',
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeedComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedComponent);
    debugElement = fixture.debugElement;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    try {
      httpMock.verify();
    } finally {
      TestBed.resetTestingModule();
    }
  });

  it('should display the spinner, then render posts after GET succeeds', () => {
    fixture.detectChanges();

    expect(debugElement.query(By.css('[data-testid="loading-screen"]'))).toBeTruthy();

    const req = httpMock.expectOne(r => r.urlWithParams === `${apiUrl}?sort=desc`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPosts);
    fixture.detectChanges();

    expect(debugElement.query(By.css('[data-testid="loading-screen"]'))).toBeNull();
    expect(debugElement.query(By.css('[data-testid="posts-list"]'))).toBeTruthy();
  });

  it('should display the error screen when GET fails', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne(r => r.urlWithParams === `${apiUrl}?sort=desc`);
    req.flush({ message: 'Server error' }, { status: 500, statusText: 'Internal Server Error' });
    fixture.detectChanges();

    const errorScreen = debugElement.query(By.css('[data-testid="error-screen"]'));
    expect(errorScreen.nativeElement.textContent).toContain('Impossible de charger les articles');
    expect(debugElement.query(By.css('[data-testid="posts-list"]'))).toBeNull();
  });
});
