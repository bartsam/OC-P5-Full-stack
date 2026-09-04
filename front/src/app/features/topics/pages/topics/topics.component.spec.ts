import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of, Subject, throwError } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { DebugElement } from '@angular/core';
import { MaterialComponents } from '../../../../shared/material';
import { NotificationService } from '../../../../shared/services/notification.service';
import { TopicsListComponent } from '../../components/list/topics-list.component';
import { TopicItem } from '../../models';
import { TopicsService } from '../../services/topics.service';
import { TopicsComponent } from './topics.component';

describe('TopicsComponent', () => {
  let component: TopicsComponent;
  let fixture: ComponentFixture<TopicsComponent>;
  let debugElement: DebugElement;
  let mockTopicsService: {
    getAllTopics: ReturnType<typeof vi.fn>;
    subscribeTopic: ReturnType<typeof vi.fn>;
  };
  let mockNotificationService: {
    success: ReturnType<typeof vi.fn>;
    error: ReturnType<typeof vi.fn>;
  };

  const mockTopics: TopicItem[] = [
    { id: 1, name: 'Java', description: 'Backend', isSubscribed: false },
    { id: 2, name: 'Angular', description: 'Frontend', isSubscribed: true },
  ];

  beforeEach(async () => {
    mockTopicsService = {
      getAllTopics: vi.fn(),
      subscribeTopic: vi.fn(),
    };
    mockNotificationService = {
      success: vi.fn(),
      error: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [MaterialComponents, TopicsComponent],
      providers: [
        { provide: TopicsService, useValue: mockTopicsService },
        { provide: NotificationService, useValue: mockNotificationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TopicsComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the spinner, then render topics after loading succeeds', () => {
    const topics$ = new Subject<TopicItem[]>();
    mockTopicsService.getAllTopics.mockReturnValue(topics$);

    fixture.detectChanges();

    expect(debugElement.query(By.css('[data-testid="loading-screen"]'))).toBeTruthy();

    topics$.next(mockTopics);
    topics$.complete();
    fixture.detectChanges();

    const topicsList = debugElement.query(By.directive(TopicsListComponent));

    expect(mockTopicsService.getAllTopics).toHaveBeenCalledTimes(1);
    expect(component.topics()).toEqual(mockTopics);
    expect(component.loading()).toBe(false);
    expect(topicsList).toBeTruthy();
    expect(debugElement.query(By.css('[data-testid="loading-screen"]'))).toBeNull();
  });

  it('should display the error and hide the topics list when loading fails', () => {
    mockTopicsService.getAllTopics.mockReturnValue(
      throwError(() => ({ error: { message: 'Server error' } })),
    );

    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.error()).toContain('Impossible de charger les topics');

    const errorMessage = debugElement.query(By.css('[data-testid="error-screen"]'));
    expect(errorMessage.nativeElement.textContent).toEqual(
      expect.stringMatching(/^Impossible de charger les topics/),
    );
    expect(debugElement.query(By.directive(TopicsListComponent))).toBeNull();
  });

  it('should update topics signal on subscribeTopic success', () => {
    const topicToSubscribe: TopicItem = {
      id: 1,
      name: 'Java',
      description: 'Backend',
      isSubscribed: false,
    };
    const initialTopics: TopicItem[] = [topicToSubscribe];

    component.topics.set(initialTopics);
    mockTopicsService.subscribeTopic.mockReturnValue(of(undefined));

    component.onSubscribe(topicToSubscribe);

    expect(mockTopicsService.subscribeTopic).toHaveBeenCalledWith(topicToSubscribe.id);
    expect(component.topics()[0].isSubscribed).toBe(true);
  });

  it('should call notificationService.error on subscribeTopic error', () => {
    const topicToSubscribe: TopicItem = {
      id: 1,
      name: 'Java',
      description: 'Backend',
      isSubscribed: false,
    };
    const initialTopics: TopicItem[] = [topicToSubscribe];

    component.topics.set(initialTopics);
    mockTopicsService.subscribeTopic.mockReturnValue(
      throwError(() => ({ error: { message: 'Forbidden' } })),
    );

    component.onSubscribe(topicToSubscribe);

    expect(mockTopicsService.subscribeTopic).toHaveBeenCalledWith(topicToSubscribe.id);
    expect(mockNotificationService.error).toHaveBeenCalledWith(
      expect.stringMatching(/^Impossible de s'abonner à ce thème/),
    );
  });
});
