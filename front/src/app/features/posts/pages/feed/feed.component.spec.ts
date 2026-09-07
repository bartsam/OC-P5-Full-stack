import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of } from 'rxjs';
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

  let mockPostsService: { getFeed: ReturnType<typeof vi.fn> };

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
    mockPostsService = { getFeed: vi.fn() };

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

  it('should call getFeed with "desc" on init', () => {
    mockPostsService.getFeed.mockReturnValue(of(mockPosts));

    fixture.detectChanges();

    expect(mockPostsService.getFeed).toHaveBeenCalledWith('desc');
    expect(component.currentSort()).toBe('desc');
  });

  it('should toggle sort between "asc" and "desc" and reload feed on each click', () => {
    mockPostsService.getFeed.mockReturnValue(of(mockPosts));
    fixture.detectChanges();

    const sortButton = debugElement.query(By.css('[data-testid="sort-button"]')).nativeElement;

    sortButton.click();
    fixture.detectChanges();
    expect(component.currentSort()).toBe('asc');
    expect(mockPostsService.getFeed).toHaveBeenLastCalledWith('asc');

    sortButton.click();
    fixture.detectChanges();
    expect(component.currentSort()).toBe('desc');
    expect(mockPostsService.getFeed).toHaveBeenLastCalledWith('desc');
  });

  it('should display create button linking to /posts/create', () => {
    mockPostsService.getFeed.mockReturnValue(of(mockPosts));

    fixture.detectChanges();

    const createButton = debugElement.query(By.css('[data-testid="create-button"]'));
    const anchor = createButton.nativeElement as HTMLAnchorElement;
    expect(anchor.getAttribute('routerLink')).toBe('/posts/create');
  });
});
