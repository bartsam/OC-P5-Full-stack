import { DebugElement } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { beforeEach, describe, expect, it } from 'vitest';

import { DatePipe } from '@angular/common';
import { MaterialComponents } from '@shared/ui/material';
import { PostItem } from '../../models';
import { PostsItemComponent } from './posts-item.component';

describe('PostsItemComponent', () => {
  let component: PostsItemComponent;
  let fixture: ComponentFixture<PostsItemComponent>;
  let debugElement: DebugElement;

  const mockPost: PostItem = {
    id: 1,
    title: 'Découvrir Spring Boot',
    content: 'Spring Boot est un framework...',
    author: 'john',
    createdAt: '2025-01-01T10:00:00',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MaterialComponents, PostsItemComponent, DatePipe],
    }).compileComponents();

    fixture = TestBed.createComponent(PostsItemComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;

    fixture.componentRef.setInput('post', mockPost);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display title, author, formatted date and content', () => {
    const title = debugElement.query(By.css('[data-testid="post-title"]'));
    const author = debugElement.query(By.css('[data-testid="post-author"]'));
    const date = debugElement.query(By.css('[data-testid="post-date"]'));
    const content = debugElement.query(By.css('[data-testid="post-content"]'));

    expect(title.nativeElement.textContent).toContain(mockPost.title);
    expect(author.nativeElement.textContent).toContain(mockPost.author);
    expect(date.nativeElement.textContent).toContain('01/01/2025');
    expect(content.nativeElement.textContent).toContain(mockPost.content);
  });

  it('should update displayed values when post input changes', () => {
    const updatedPost: PostItem = {
      id: 2,
      title: 'Bien démarrer avec Angular',
      content: 'Angular permet de construire des applications web.',
      author: 'alice',
      createdAt: '2025-02-02T14:30:00',
    };

    fixture.componentRef.setInput('post', updatedPost);
    fixture.detectChanges();

    const title = debugElement.query(By.css('[data-testid="post-title"]'));
    const author = debugElement.query(By.css('[data-testid="post-author"]'));
    const date = debugElement.query(By.css('[data-testid="post-date"]'));
    const content = debugElement.query(By.css('[data-testid="post-content"]'));

    expect(title.nativeElement.textContent).toContain(updatedPost.title);
    expect(author.nativeElement.textContent).toContain(updatedPost.author);
    expect(date.nativeElement.textContent).toContain('02/02/2025');
    expect(content.nativeElement.textContent).toContain(updatedPost.content);
  });
});
