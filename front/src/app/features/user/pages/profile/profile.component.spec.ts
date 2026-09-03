import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { MaterialComponents } from '../../../../shared/material';
import { NotificationService } from '../../../../shared/services/notification.service';
import { User } from '../../models';
import { UserService } from '../../services/user.service';
import { ProfileComponent } from './profile.component';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let mockUserService: {
    getUser: ReturnType<typeof vi.fn>;
    updateUser: ReturnType<typeof vi.fn>;
  };
  let mockNotificationService: {
    success: ReturnType<typeof vi.fn>;
    error: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    mockUserService = {
      getUser: vi.fn(),
      updateUser: vi.fn(),
    };

    mockNotificationService = {
      success: vi.fn(),
      error: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, MaterialComponents, ProfileComponent],
      providers: [
        { provide: UserService, useValue: mockUserService },
        { provide: NotificationService, useValue: mockNotificationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
  });

  describe('getUser', () => {
    it('should populate form and signals on success', () => {
      const mockUser: User = {
        id: 1,
        email: 'john.doe@example.com',
        username: 'jeanbiche',
        createdAt: '2026-08-31T10:00:00Z',
        updatedAt: '2026-08-31T10:00:00Z',
      };

      mockUserService.getUser.mockReturnValue(of(mockUser));
      component.ngOnInit();
      fixture.detectChanges();

      expect(mockUserService.getUser).toHaveBeenCalled();
      expect(component.user()).toEqual(mockUser);
      expect(component.isLoading()).toBe(false);
      expect(component.errorMessage()).toBeNull();

      expect(component.form.value.email).toBe('john.doe@example.com');
      expect(component.form.value.username).toBe('jeanbiche');
    });

    it('should set errorMessage on getUser error', () => {
      mockUserService.getUser.mockReturnValue(throwError(() => new Error('HTTP error')));

      component.ngOnInit();
      fixture.detectChanges();

      expect(mockUserService.getUser).toHaveBeenCalled();
      expect(component.isLoading()).toBe(false);
      expect(component.errorMessage()).toEqual(
        expect.stringMatching(/^Impossible de charger le profil/),
      );
    });
  });

  describe('updateUser', () => {
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
      mockUserService.updateUser.mockReturnValue(of({}));

      component.submit();

      expect(mockUserService.updateUser).toHaveBeenCalledWith(updateRequest);
      expect(component.isLoading()).toBe(false);
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
      mockUserService.updateUser.mockReturnValue(throwError(() => new Error('HTTP error')));

      component.submit();

      expect(mockUserService.updateUser).toHaveBeenCalledWith(updateRequest);
      expect(component.isLoading()).toBe(false);
      expect(mockNotificationService.error).toHaveBeenCalledWith(
        expect.stringMatching(/^Impossible de modifier le profil/),
      );
    });

    it('should not call updateUser when form is invalid', () => {
      component.submit();

      expect(mockUserService.updateUser).not.toHaveBeenCalled();
    });
  });
});
