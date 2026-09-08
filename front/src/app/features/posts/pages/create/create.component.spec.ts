import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { Router } from '@angular/router';
import { NotificationService } from '@shared/services/notification.service';
import { MaterialComponents } from '@shared/ui/material';
import { TopicOption } from '../../../topics/models';
import { TopicsService } from '../../../topics/services/topics.service';
import { PostsService } from '../../services/posts.service';
import { PostCreateComponent } from './create.component';

describe('PostCreateComponent', () => {
  let component: PostCreateComponent;
  let fixture: ComponentFixture<PostCreateComponent>;

  let mockPostsService: { createPost: ReturnType<typeof vi.fn> };
  let mockTopicsService: { getTopicOptions: ReturnType<typeof vi.fn> };
  let mockRouter: { navigate: ReturnType<typeof vi.fn> };
  let mockNotificationService: {
    success: ReturnType<typeof vi.fn>;
    error: ReturnType<typeof vi.fn>;
  };

  const mockTopics: TopicOption[] = [
    { id: 1, name: 'Java' },
    { id: 2, name: 'Angular' },
  ];

  beforeEach(async () => {
    mockPostsService = { createPost: vi.fn() };
    mockTopicsService = { getTopicOptions: vi.fn().mockReturnValue(of(mockTopics)) };
    mockRouter = { navigate: vi.fn() };
    mockNotificationService = { success: vi.fn(), error: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [MaterialComponents, PostCreateComponent],
      providers: [
        { provide: PostsService, useValue: mockPostsService },
        { provide: TopicsService, useValue: mockTopicsService },
        { provide: Router, useValue: mockRouter },
        { provide: NotificationService, useValue: mockNotificationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PostCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have an invalid form when fields are empty', () => {
    expect(component.form.invalid).toBe(true);
  });

  it('should have a valid form when all fields are correctly filled', () => {
    component.form.setValue({ topicId: 1, title: 'Titre valide', content: 'Contenu valide' });
    expect(component.form.valid).toBe(true);
  });

  it('should not call createPost when submit is triggered with an invalid form', () => {
    component.submit();
    expect(mockPostsService.createPost).not.toHaveBeenCalled();
  });
});
