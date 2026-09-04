import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { MaterialComponents } from '../../../../shared/material';
import { NotificationService } from '../../../../shared/services/notification.service';
import { TopicItem } from '../../models';
import { TopicsService } from '../../services/topics.service';
import { TopicsComponent } from './topics.component';

describe('TopicsComponent', () => {
  let component: TopicsComponent;
  let fixture: ComponentFixture<TopicsComponent>;
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
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load topics on init and update signals', () => {
    mockTopicsService.getAllTopics.mockReturnValue(of(mockTopics));

    component.ngOnInit();
    fixture.detectChanges();

    expect(mockTopicsService.getAllTopics).toHaveBeenCalled();
    expect(component.topics()).toEqual(mockTopics);
    expect(component.loading()).toBe(false);
    expect(component.error()).toBeNull();
  });

  it('should set error signal on getAllTopics error', () => {
    mockTopicsService.getAllTopics.mockReturnValue(
      throwError(() => ({ error: { message: 'Server error' } })),
    );

    component.ngOnInit();
    fixture.detectChanges();

    expect(mockTopicsService.getAllTopics).toHaveBeenCalled();
    expect(component.loading()).toBe(false);
    expect(component.error()).toEqual(expect.stringMatching(/^Impossible de charger les topics/));
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
