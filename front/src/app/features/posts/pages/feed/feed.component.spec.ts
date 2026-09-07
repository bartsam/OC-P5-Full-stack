import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of, Subject, throwError } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { DebugElement } from '@angular/core';
import { provideRouter } from '@angular/router';
import { MaterialComponents } from '@shared/ui/material';
import { PostItem } from '../../models';
import { PostsService } from '../../services/posts.service';
import { FeedComponent } from './feed.component';

describe('FeedComponent', () => {
  let component: FeedComponent;
  let fixture: ComponentFixture<FeedComponent>;
  let debugElement: DebugElement;

  let mockPostsService: {
    getFeed: ReturnType<typeof vi.fn>;
  };

  const mockPosts: PostItem[] = [
    {
      id: 1,
      title: 'Old post',
      content: 'Old content…',
      author: 'User 1',
      createdAt: '2025-01-01T10:00:00',
    },
    {
      id: 2,
      title: 'New post',
      content: 'New content…',
      author: 'User 2',
      createdAt: '2025-01-02T10:00:00',
    },
  ];

  beforeEach(async () => {
    mockPostsService = {
      getFeed: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [MaterialComponents, FeedComponent],
      providers: [{ provide: PostsService, useValue: mockPostsService }, provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the spinner, then render posts after loading succeeds', () => {
    const posts$ = new Subject<PostItem[]>();
    mockPostsService.getFeed.mockReturnValue(posts$);

    fixture.detectChanges();

    expect(debugElement.query(By.css('[data-testid="loading-screen"]'))).toBeTruthy();

    posts$.next(mockPosts);
    posts$.complete();
    fixture.detectChanges();

    expect(mockPostsService.getFeed).toHaveBeenCalledTimes(1);
    expect(component.posts()).toEqual(mockPosts);
    expect(component.loading()).toBe(false);
    expect(debugElement.query(By.css('[data-testid="loading-screen"]'))).toBeNull();
    expect(debugElement.query(By.css('[data-testid="posts-list"]'))).toBeTruthy();
  });

  it('should display the error and hide the posts list when loading fails', () => {
    mockPostsService.getFeed.mockReturnValue(
      throwError(() => ({ error: { message: 'Server error' } })),
    );

    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.error()).toContain('Impossible de charger les articles');

    const errorMessage = debugElement.query(By.css('[data-testid="error-screen"]'));
    expect(errorMessage.nativeElement.textContent).toEqual(
      expect.stringMatching(/^Impossible de charger les articles/),
    );

    expect(debugElement.query(By.css('[data-testid="posts-list"]'))).toBeNull();
  });

  it('should call getFeed with "desc" on init and display posts', () => {
    mockPostsService.getFeed.mockReturnValue(of(mockPosts));

    fixture.detectChanges();

    expect(mockPostsService.getFeed).toHaveBeenCalledWith('desc');
    expect(component.posts()).toEqual(mockPosts);
    expect(component.loading()).toBe(false);
  });

  it('should toggle sort from "desc" to "asc" and reload feed when sort button is clicked', () => {
    mockPostsService.getFeed.mockReturnValue(of(mockPosts));

    fixture.detectChanges();

    expect(component.currentSort()).toBe('desc');
    expect(mockPostsService.getFeed).toHaveBeenLastCalledWith('desc');

    const sortButton = debugElement.query(By.css('[data-testid="sort-button"]')).nativeElement;
    sortButton.click();
    fixture.detectChanges();

    expect(component.currentSort()).toBe('asc');
    expect(mockPostsService.getFeed).toHaveBeenLastCalledWith('asc');
    expect(component.posts()).toEqual(mockPosts);
    expect(component.loading()).toBe(false);
  });

  it('should toggle sort from "asc" to "desc" on second click', () => {
    mockPostsService.getFeed.mockReturnValue(of(mockPosts));

    fixture.detectChanges();
    expect(component.currentSort()).toBe('desc');

    const sortButton = debugElement.query(By.css('[data-testid="sort-button"]')).nativeElement;
    sortButton.click();
    fixture.detectChanges();
    expect(component.currentSort()).toBe('asc');

    sortButton.click();
    fixture.detectChanges();
    expect(component.currentSort()).toBe('desc');
    expect(mockPostsService.getFeed).toHaveBeenLastCalledWith('desc');
  });

  it('should display error and hide posts list when sort fails', () => {
    mockPostsService.getFeed.mockReturnValue(
      throwError(() => ({ error: { message: 'Forbidden' } })),
    );

    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.error()).toContain('Impossible de charger les articles');

    const errorMessage = debugElement.query(By.css('[data-testid="error-screen"]'));
    expect(errorMessage.nativeElement.textContent).toEqual(
      expect.stringMatching(/^Impossible de charger les articles/),
    );

    expect(debugElement.query(By.css('[data-testid="posts-list"]'))).toBeNull();
  });

  it('should display create button that links to /posts/create', () => {
    mockPostsService.getFeed.mockReturnValue(of(mockPosts));

    fixture.detectChanges();

    const createButton = debugElement.query(By.css('[data-testid="create-button"]'));
    expect(createButton).toBeTruthy();

    const anchor = createButton.nativeElement as HTMLAnchorElement;
    expect(anchor.getAttribute('routerLink')).toBe('/posts/create');
  });
});
