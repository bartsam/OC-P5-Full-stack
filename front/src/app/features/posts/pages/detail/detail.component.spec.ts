import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of, Subject, throwError } from 'rxjs';
import { afterEach, describe, expect, it, vi } from 'vitest';

import { DebugElement } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { MaterialComponents } from '@shared/ui/material';
import { PostDetail } from '../../models';
import { PostsService } from '../../services/posts.service';
import { PostDetailComponent } from './detail.component';

describe('PostDetailComponent', () => {
  let component: PostDetailComponent;
  let fixture: ComponentFixture<PostDetailComponent>;
  let debugElement: DebugElement;

  let mockPostsService: {
    getPost: ReturnType<typeof vi.fn>;
  };

  let mockTitleService: {
    setTitle: ReturnType<typeof vi.fn>;
  };

  const mockPost: PostDetail = {
    id: 123,
    title: 'Bien démarrer avec React Native et Expo',
    content: 'React Native permet de développer des applications mobiles…',
    author: 'jean-biche',
    topic: 'Mobile',
    createdAt: '2026-09-07T15:39:00',
  };

  function setupTestBed(activatedRouteIdMock: string) {
    mockPostsService = {
      getPost: vi.fn(),
    };

    mockTitleService = {
      setTitle: vi.fn(),
    };

    TestBed.configureTestingModule({
      imports: [MaterialComponents, PostDetailComponent],
      providers: [
        { provide: PostsService, useValue: mockPostsService },
        { provide: Title, useValue: mockTitleService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ id: activatedRouteIdMock }),
            },
          },
        },
      ],
    });

    fixture = TestBed.createComponent(PostDetailComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
  }

  afterEach(() => {
    vi.clearAllMocks();
    TestBed.resetTestingModule();
  });

  it('should create', () => {
    setupTestBed('123');
    expect(component).toBeTruthy();
  });

  it('should display the spinner, then render the post after loading succeeds', () => {
    setupTestBed('123');

    const postSubject = new Subject<PostDetail>();
    mockPostsService.getPost.mockReturnValue(postSubject.asObservable());

    fixture.detectChanges();

    expect(debugElement.query(By.css('[data-testid="loading-screen"]'))).toBeTruthy();

    postSubject.next(mockPost);
    postSubject.complete();
    fixture.detectChanges();

    expect(mockPostsService.getPost).toHaveBeenCalledWith('123');
    expect(component.post()).toEqual(mockPost);
    expect(component.loading()).toBe(false);
    expect(component.error()).toBeNull();

    expect(debugElement.query(By.css('[data-testid="loading-screen"]'))).toBeNull();
    expect(debugElement.query(By.css('[data-testid="post-title"]'))).toBeTruthy();
    expect(debugElement.query(By.css('[data-testid="post-content"]'))).toBeTruthy();

    expect(mockTitleService.setTitle).toHaveBeenCalledWith(`${mockPost.title} - MDD`);
  });

  it('should display the error and hide the post when loading fails', () => {
    setupTestBed('123');

    mockPostsService.getPost.mockReturnValue(
      throwError(() => ({ error: { message: 'Not found' } })),
    );

    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.error()).toContain("Impossible de charger l'article");

    const errorMessage = debugElement.query(By.css('[data-testid="error-screen"]'));
    expect(errorMessage).toBeTruthy();
    expect(errorMessage!.nativeElement.textContent).toEqual(
      expect.stringMatching(/^Impossible de charger l'article/),
    );

    expect(debugElement.query(By.css('[data-testid="post-title"]'))).toBeNull();
    expect(debugElement.query(By.css('[data-testid="post-content"]'))).toBeNull();
  });

  it('should display an error when route id is missing', () => {
    setupTestBed('');

    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.error()).toEqual('Identifiant de l’article invalide.');

    const errorMessage = debugElement.query(By.css('[data-testid="error-screen"]'));
    expect(errorMessage).toBeTruthy();
    expect(errorMessage!.nativeElement.textContent).toContain('Identifiant de l’article invalide');

    expect(debugElement.query(By.css('[data-testid="post-title"]'))).toBeNull();
    expect(mockPostsService.getPost).not.toHaveBeenCalled();
  });

  it('should call getPost with the correct id on init', () => {
    setupTestBed('123');

    mockPostsService.getPost.mockReturnValue(of(mockPost));

    fixture.detectChanges();

    expect(mockPostsService.getPost).toHaveBeenCalledWith('123');
    expect(component.postId()).toBe('123');
  });
});
