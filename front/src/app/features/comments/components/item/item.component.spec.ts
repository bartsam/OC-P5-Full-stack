import { DebugElement } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { beforeEach, describe, expect, it } from 'vitest';

import { MaterialComponents } from '@shared/ui/material';
import { CommentItem } from '../../models';
import { CommentsItemComponent } from './item.component';

describe('CommentsItemComponent', () => {
  let component: CommentsItemComponent;
  let fixture: ComponentFixture<CommentsItemComponent>;
  let debugElement: DebugElement;

  const mockComment: CommentItem = {
    id: 1,
    author: 'User 1',
    content: 'Comment 1',
    createdAt: '2026-09-07T15:39:00',
  } as CommentItem;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MaterialComponents, CommentsItemComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(CommentsItemComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    fixture.componentRef.setInput('comment', mockComment);
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should display the author', () => {
    fixture.detectChanges();

    const authorEl = debugElement.query(By.css('[data-testid="post-author"]'));
    expect((authorEl.nativeElement as HTMLElement).textContent?.trim()).toBe('User 1');
  });

  it('should display the content', () => {
    fixture.detectChanges();

    const contentEl = debugElement.query(By.css('[data-testid="post-content"]'));
    expect((contentEl.nativeElement as HTMLElement).textContent?.trim()).toBe('Comment 1');
  });

  it('should update the displayed values when the comment input changes', () => {
    fixture.detectChanges();

    const updatedComment: CommentItem = {
      id: 2,
      author: 'User 2',
      content: 'Comment 2',
      createdAt: '2025-06-01T08:15:00',
    } as CommentItem;

    fixture.componentRef.setInput('comment', updatedComment);
    fixture.detectChanges();

    const authorEl = debugElement.query(By.css('[data-testid="post-author"]'));
    const contentEl = debugElement.query(By.css('[data-testid="post-content"]'));

    expect((authorEl.nativeElement as HTMLElement).textContent?.trim()).toBe('User 2');
    expect((contentEl.nativeElement as HTMLElement).textContent?.trim()).toBe('Comment 2');
  });
});
