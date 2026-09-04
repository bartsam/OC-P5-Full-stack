import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { of, Subject, throwError } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { DebugElement } from '@angular/core';
import { MaterialComponents } from '../../../../shared/material';
import { NotificationService } from '../../../../shared/services/notification.service';
import { TopicsListComponent } from '../../../topics/components/list/topics-list.component';
import { TopicItem } from '../../../topics/models';
import { TopicsService } from '../../../topics/services/topics.service';
import { User } from '../../models';
import { UserService } from '../../services/user.service';
import { ProfileComponent } from './profile.component';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let debugElement: DebugElement;
  let mockUserService: {
    getUser: ReturnType<typeof vi.fn>;
    updateUser: ReturnType<typeof vi.fn>;
  };
  let mockTopicsService: {
    getSubscribedTopics: ReturnType<typeof vi.fn>;
    unSubscribeTopic: ReturnType<typeof vi.fn>;
  };
  let mockNotificationService: {
    success: ReturnType<typeof vi.fn>;
    error: ReturnType<typeof vi.fn>;
  };

  const mockUser: User = {
    id: 1,
    email: 'john.doe@example.com',
    username: 'jeanbiche',
    createdAt: '2026-08-31T10:00:00Z',
    updatedAt: '2026-08-31T10:00:00Z',
  };

  const mockTopics: TopicItem[] = [
    { id: 1, name: 'Java', description: 'Backend', isSubscribed: true },
    { id: 2, name: 'Angular', description: 'Frontend', isSubscribed: true },
  ];

  beforeEach(async () => {
    mockUserService = {
      getUser: vi.fn(),
      updateUser: vi.fn(),
    };

    mockTopicsService = {
      getSubscribedTopics: vi.fn(),
      unSubscribeTopic: vi.fn(),
    };

    mockNotificationService = {
      success: vi.fn(),
      error: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, MaterialComponents, ProfileComponent],
      providers: [
        { provide: UserService, useValue: mockUserService },
        { provide: TopicsService, useValue: mockTopicsService },
        { provide: NotificationService, useValue: mockNotificationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('ngOnInit', () => {
    it('should display the spinner, then render the profile and subscribed topics', () => {
      const user$ = new Subject<User>();
      const topics$ = new Subject<TopicItem[]>();
      mockUserService.getUser.mockReturnValue(user$);
      mockTopicsService.getSubscribedTopics.mockReturnValue(topics$);

      fixture.detectChanges();

      expect(debugElement.query(By.css('[data-testid="loading-screen"]'))).toBeTruthy();

      user$.next(mockUser);
      user$.complete();
      topics$.next(mockTopics);
      topics$.complete();
      fixture.detectChanges();

      const topicsList = debugElement.query(By.directive(TopicsListComponent));
      expect(mockUserService.getUser).toHaveBeenCalledTimes(1);
      expect(mockTopicsService.getSubscribedTopics).toHaveBeenCalledTimes(1);
      expect(component.user()).toEqual(mockUser);
      expect(component.topics()).toEqual(mockTopics);
      expect(component.loading()).toBe(false);
      expect(component.error()).toBeNull();
      expect(component.form.value.email).toBe('john.doe@example.com');
      expect(component.form.value.username).toBe('jeanbiche');
      expect(topicsList).toBeTruthy();
      expect(debugElement.query(By.css('mat-spinner'))).toBeNull();
    });

    it('should display an error and hide profile content when loading the user fails', () => {
      mockUserService.getUser.mockReturnValue(
        throwError(() => ({ error: { message: 'Not found' } })),
      );
      mockTopicsService.getSubscribedTopics.mockReturnValue(of(mockTopics));

      fixture.detectChanges();

      const errorMessage = debugElement.query(By.css('[data-testid="error-screen"]'));
      expect(component.loading()).toBe(false);
      expect(component.error()).toContain('Impossible de charger le profil');
      expect(errorMessage.nativeElement.textContent).toContain(
        'Impossible de charger le profil : Not found',
      );
      expect(debugElement.query(By.css('form'))).toBeNull();
    });

    it('should display an error and hide profile content when loading topics fails', () => {
      mockUserService.getUser.mockReturnValue(of(mockUser));
      mockTopicsService.getSubscribedTopics.mockReturnValue(
        throwError(() => ({ error: { message: 'Server error' } })),
      );

      fixture.detectChanges();

      const errorMessage = debugElement.query(By.css('[data-testid="error-screen"]'));
      expect(component.loading()).toBe(false);
      expect(component.error()).toContain('Impossible de charger le thème');
      expect(errorMessage.nativeElement.textContent).toContain(
        'Impossible de charger le thème : Server error',
      );
      expect(debugElement.query(By.css('form'))).toBeNull();
    });
  });

  describe('submit', () => {
    it('should have an invalid form when empty', () => {
      expect(component.form.valid).toBe(false);
    });

    it('should update user, refresh signal and notify success when form is valid', () => {
      const updateRequest = {
        email: 'new.john@example.com',
        username: 'newjeanbiche',
        password: 'NewPassword123!',
      };

      component.form.setValue(updateRequest);
      mockUserService.updateUser.mockReturnValue(of(mockUser));

      component.submit();

      expect(mockUserService.updateUser).toHaveBeenCalledWith(updateRequest);
      expect(component.user()).toEqual(mockUser);
      expect(component.loading()).toBe(false);
      expect(mockNotificationService.success).toHaveBeenCalledWith(
        'Profil mis à jour avec succès.',
      );
    });

    it('should notify error on updateUser error', () => {
      const updateRequest = {
        email: 'new.john@example.com',
        username: 'newjeanbiche',
        password: 'NewPassword123!',
      };

      component.form.setValue(updateRequest);
      mockUserService.updateUser.mockReturnValue(
        throwError(() => ({ error: { message: 'Conflict' } })),
      );

      component.submit();

      expect(mockUserService.updateUser).toHaveBeenCalledWith(updateRequest);
      expect(component.loading()).toBe(false);
      expect(mockNotificationService.error).toHaveBeenCalledWith(
        expect.stringMatching(/^Impossible de modifier le profil/),
      );
    });

    it('should not call updateUser when form is invalid', () => {
      component.submit();

      expect(mockUserService.updateUser).not.toHaveBeenCalled();
    });
  });

  describe('onUnsubscribe', () => {
    it('should remove topic from topics signal on success', () => {
      const topicToRemove: TopicItem = {
        id: 1,
        name: 'Java',
        description: 'Backend',
        isSubscribed: true,
      };
      const remainingTopics: TopicItem[] = [
        { id: 2, name: 'Angular', description: 'Frontend', isSubscribed: true },
      ];

      component.topics.set([topicToRemove, ...remainingTopics]);
      mockTopicsService.unSubscribeTopic.mockReturnValue(of(undefined));

      component.onUnsubscribe(topicToRemove);

      expect(mockTopicsService.unSubscribeTopic).toHaveBeenCalledWith(topicToRemove.id);
      expect(component.topics()).toEqual(remainingTopics);
    });

    it('should not update topics signal on error', () => {
      const topicToRemove: TopicItem = {
        id: 1,
        name: 'Java',
        description: 'Backend',
        isSubscribed: true,
      };
      const initialTopics: TopicItem[] = [topicToRemove];

      component.topics.set(initialTopics);
      mockTopicsService.unSubscribeTopic.mockReturnValue(
        throwError(() => ({ error: { message: 'Forbidden' } })),
      );

      component.onUnsubscribe(topicToRemove);

      expect(mockTopicsService.unSubscribeTopic).toHaveBeenCalledWith(topicToRemove.id);
      expect(component.topics()).toEqual(initialTopics);
    });

    it('should call notificationService.error on unSubscribeTopic error', () => {
      const topicToRemove: TopicItem = {
        id: 1,
        name: 'Java',
        description: 'Backend',
        isSubscribed: true,
      };
      const initialTopics: TopicItem[] = [topicToRemove];

      component.topics.set(initialTopics);
      mockTopicsService.unSubscribeTopic.mockReturnValue(
        throwError(() => ({ error: { message: 'Forbidden' } })),
      );

      component.onUnsubscribe(topicToRemove);

      expect(mockTopicsService.unSubscribeTopic).toHaveBeenCalledWith(topicToRemove.id);
      expect(mockNotificationService.error).toHaveBeenCalledWith(
        expect.stringMatching(/^Impossible de se désabonner de ce thème/),
      );
      expect(component.topics()).toEqual(initialTopics);
    });
  });
});
