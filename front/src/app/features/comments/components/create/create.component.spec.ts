import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { NotificationService } from '@shared/services/notification.service';
import { MaterialComponents } from '@shared/ui/material';
import { CommentItem } from '../../models';
import { CommentsService } from '../../services/comments.service';
import { CommentCreateComponent } from './create.component';

describe('CommentCreateComponent', () => {
  let component: CommentCreateComponent;
  let fixture: ComponentFixture<CommentCreateComponent>;

  let mockCommentsService: {
    createComment: ReturnType<typeof vi.fn>;
  };

  let mockNotificationService: {
    success: ReturnType<typeof vi.fn>;
    error: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    mockCommentsService = {
      createComment: vi.fn(),
    };

    mockNotificationService = {
      success: vi.fn(),
      error: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [MaterialComponents, ReactiveFormsModule, CommentCreateComponent],
      providers: [
        { provide: CommentsService, useValue: mockCommentsService },
        { provide: NotificationService, useValue: mockNotificationService },
        FormBuilder,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CommentCreateComponent);
    component = fixture.componentInstance;

    fixture.componentRef.setInput('postId', 1);
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with empty content', () => {
    expect(component.form).toBeDefined();
    expect(component.form.controls.content.value).toBe('');
    expect(component.form.controls.content.valid).toBe(false);
  });

  it('should mark the form as invalid when content is empty', () => {
    component.form.controls.content.setValue('');
    expect(component.form.invalid).toBe(true);
    expect(component.form.controls.content.errors?.['required']).toBeTruthy();
  });

  it('should mark the form as valid when content is filled', () => {
    component.form.controls.content.setValue('A valid comment');
    expect(component.form.valid).toBe(true);
  });

  it('should call createComment with correct args and show success notification when submission succeeds', () => {
    const mockResponse: CommentItem = {
      id: 1,
      author: 'jeanbiche',
      content: 'Great article !',
      createdAt: '2026-09-08T12:00:00',
    };

    mockCommentsService.createComment.mockReturnValue(of(mockResponse));

    component.form.controls.content.setValue('Great article !');
    component.submit();
    fixture.detectChanges();

    expect(mockCommentsService.createComment).toHaveBeenCalledWith(
      { content: 'Great article !' },
      1,
    );
    expect(mockNotificationService.success).toHaveBeenCalledWith('Commentaire créé avec succès.');
    expect(component.loading()).toBe(false);
    expect(component.error()).toBeNull();
  });

  it('should show error notification and keep loading false when submission fails', () => {
    mockCommentsService.createComment.mockReturnValue(
      throwError(() => ({ error: { message: 'Bad Request' } })),
    );

    component.form.controls.content.setValue('A comment');
    component.submit();
    fixture.detectChanges();

    expect(mockCommentsService.createComment).toHaveBeenCalled();
    expect(mockNotificationService.error).toHaveBeenCalledWith(
      expect.stringMatching(/^Impossible de créer le commentaire/),
    );
    expect(component.loading()).toBe(false);
  });

  it('should not call createComment when form is invalid', () => {
    component.form.controls.content.setValue('');
    component.submit();
    fixture.detectChanges();

    expect(mockCommentsService.createComment).not.toHaveBeenCalled();
    expect(mockNotificationService.success).not.toHaveBeenCalled();
    expect(mockNotificationService.error).not.toHaveBeenCalled();
  });
});
