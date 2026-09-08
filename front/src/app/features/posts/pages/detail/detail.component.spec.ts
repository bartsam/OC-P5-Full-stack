import { Component, DebugElement, Input } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By, Title } from '@angular/platform-browser';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { MaterialComponents } from '@shared/ui/material';
import { of } from 'rxjs';
import { afterEach, describe, expect, it, vi } from 'vitest';

import { CommentCreateComponent } from '../../../comments/components/create/create.component';
import { CommentsListComponent } from '../../../comments/components/list/list.component';
import { PostDetail } from '../../models';
import { PostsService } from '../../services/posts.service';
import { PostDetailComponent } from './detail.component';

@Component({
  selector: 'app-comments-list',
  standalone: true,
  template: '',
})
class CommentsListStubComponent {
  @Input() postId!: number;
}

@Component({
  selector: 'app-create-comment',
  standalone: true,
  template: '',
})
class CommentCreateStubComponent {
  @Input() postId!: number;
}

describe('PostDetailComponent', () => {
  let component: PostDetailComponent;
  let fixture: ComponentFixture<PostDetailComponent>;
  let debugElement: DebugElement;

  let mockPostsService: { getPost: ReturnType<typeof vi.fn> };
  let mockTitleService: { setTitle: ReturnType<typeof vi.fn> };

  const mockPost: PostDetail = {
    id: 123,
    title: 'Bien démarrer avec React Native et Expo',
    content: 'React Native permet de développer des applications mobiles…',
    author: 'jean-biche',
    topic: 'Mobile',
    createdAt: '2026-09-07T15:39:00',
  };

  function setupTestBed(activatedRouteIdMock: string) {
    mockPostsService = { getPost: vi.fn() };
    mockTitleService = { setTitle: vi.fn() };

    TestBed.configureTestingModule({
      imports: [MaterialComponents, PostDetailComponent],
      providers: [
        { provide: PostsService, useValue: mockPostsService },
        { provide: Title, useValue: mockTitleService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: convertToParamMap({ id: activatedRouteIdMock }) },
          },
        },
      ],
    });

    TestBed.overrideComponent(PostDetailComponent, {
      remove: { imports: [CommentsListComponent, CommentCreateComponent] },
      add: { imports: [CommentsListStubComponent, CommentCreateStubComponent] },
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

  it('should set postId and call getPost with the route id on init', () => {
    setupTestBed('123');
    mockPostsService.getPost.mockReturnValue(of(mockPost));

    fixture.detectChanges();

    expect(component.postId()).toBe('123');
    expect(mockPostsService.getPost).toHaveBeenCalledWith('123');
  });

  it('should set an error and skip getPost when route id is missing', () => {
    setupTestBed('');

    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.error()).toEqual('Identifiant de l’article invalide.');
    expect(mockPostsService.getPost).not.toHaveBeenCalled();

    const errorMessage = debugElement.query(By.css('[data-testid="error-screen"]'));
    expect(errorMessage.nativeElement.textContent).toContain('Identifiant de l’article invalide.');
  });

  it('should render comments list and create-comment with the post id once loaded', () => {
    setupTestBed('123');
    mockPostsService.getPost.mockReturnValue(of(mockPost));

    fixture.detectChanges();

    const commentsList = debugElement.query(By.directive(CommentsListStubComponent));
    const commentCreate = debugElement.query(By.directive(CommentCreateStubComponent));

    expect(commentsList).toBeTruthy();
    expect(commentCreate).toBeTruthy();
    expect((commentsList.componentInstance as CommentsListStubComponent).postId).toBe(mockPost.id);
    expect((commentCreate.componentInstance as CommentCreateStubComponent).postId).toBe(
      mockPost.id,
    );
  });

  it('should not render comments components while loading', () => {
    setupTestBed('123');
    mockPostsService.getPost.mockReturnValue(of(mockPost));

    expect(debugElement.query(By.directive(CommentsListStubComponent))).toBeNull();
    expect(debugElement.query(By.directive(CommentCreateStubComponent))).toBeNull();
  });

  it('should not render comments components on error', () => {
    setupTestBed('');

    fixture.detectChanges();

    expect(debugElement.query(By.directive(CommentsListStubComponent))).toBeNull();
    expect(debugElement.query(By.directive(CommentCreateStubComponent))).toBeNull();
  });
});
