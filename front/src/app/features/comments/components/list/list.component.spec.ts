import { HttpErrorResponse } from '@angular/common/http';
import { DebugElement } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of, Subject, throwError } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { MaterialComponents } from '@shared/ui/material';
import { CommentItem } from '../../models';
import { CommentsService } from '../../services/comments.service';
import { CommentsListComponent } from './list.component';

describe('CommentsListComponent', () => {
  let component: CommentsListComponent;
  let fixture: ComponentFixture<CommentsListComponent>;
  let debugElement: DebugElement;

  let mockCommentsService: { getComments: ReturnType<typeof vi.fn> };

  const mockComments: CommentItem[] = [
    {
      id: 1,
      content: 'Comment 1',
      author: 'User 1',
      createdAt: '2025-01-01T10:00:00',
    },
    {
      id: 2,
      content: 'Comment 2',
      author: 'User 2',
      createdAt: '2025-01-02T10:00:00',
    },
  ] as CommentItem[];

  beforeEach(async () => {
    mockCommentsService = { getComments: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [MaterialComponents, CommentsListComponent],
      providers: [{ provide: CommentsService, useValue: mockCommentsService }],
    }).compileComponents();

    fixture = TestBed.createComponent(CommentsListComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    fixture.componentRef.setInput('postId', 42);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    mockCommentsService.getComments.mockReturnValue(of([]));
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should call getComments with the postId input on init', () => {
    mockCommentsService.getComments.mockReturnValue(of(mockComments));

    fixture.detectChanges();

    expect(mockCommentsService.getComments).toHaveBeenCalledWith(42);
  });

  it('should show the loading spinner while the request is pending', () => {
    const subject = new Subject<CommentItem[]>();
    mockCommentsService.getComments.mockReturnValue(subject.asObservable());

    fixture.detectChanges();

    expect(component.loading()).toBe(true);
    const spinner = debugElement.query(By.css('[data-testid="loading-screen"]'));
    expect(spinner).toBeTruthy();
    expect(debugElement.query(By.css('[data-testid="posts-list"]'))).toBeFalsy();
    expect(debugElement.query(By.css('[data-testid="error-screen"]'))).toBeFalsy();
  });

  it('should display the comments list on success and hide the spinner', () => {
    mockCommentsService.getComments.mockReturnValue(of(mockComments));

    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.comments()).toEqual(mockComments);
    expect(component.error()).toBeNull();

    const list = debugElement.query(By.css('[data-testid="posts-list"]'));
    expect(list).toBeTruthy();
    expect(debugElement.query(By.css('[data-testid="loading-screen"]'))).toBeFalsy();

    const items = debugElement.queryAll(By.css('app-comments-item'));
    expect(items.length).toBe(mockComments.length);
  });

  it('should display an empty list container when there are no comments', () => {
    mockCommentsService.getComments.mockReturnValue(of([]));

    fixture.detectChanges();

    expect(debugElement.query(By.css('[data-testid="posts-list"]'))).toBeTruthy();
    expect(debugElement.queryAll(By.css('app-comments-item')).length).toBe(0);
  });

  it('should display an error message and hide the spinner on failure', () => {
    const httpError = new HttpErrorResponse({
      error: { message: 'Post introuvable' },
      status: 404,
    });
    mockCommentsService.getComments.mockReturnValue(throwError(() => httpError));

    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.error()).toBe('Impossible de charger les commentaire : Post introuvable');

    const errorEl = debugElement.query(By.css('[data-testid="error-screen"]'));
    expect(errorEl).toBeTruthy();
    expect((errorEl.nativeElement as HTMLElement).textContent).toContain('Post introuvable');
    expect(debugElement.query(By.css('[data-testid="loading-screen"]'))).toBeFalsy();
    expect(debugElement.query(By.css('[data-testid="posts-list"]'))).toBeFalsy();
  });

  it('should handle an error with no message body gracefully', () => {
    const httpError = new HttpErrorResponse({ status: 500 });
    mockCommentsService.getComments.mockReturnValue(throwError(() => httpError));

    fixture.detectChanges();

    expect(component.error()).toBe('Impossible de charger les commentaire : undefined');
  });
});
