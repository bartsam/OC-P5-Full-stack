import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';

import { provideHttpClient } from '@angular/common/http';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { environment } from '../../../../../environments/environment';
import { TopicItem } from '../../../topics/models';
import { User } from '../../models';
import { ProfileComponent } from './profile.component';

describe('ProfileComponent integration', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let debugElement: DebugElement;
  let httpMock: HttpTestingController;
  let profileApiUrl: string;
  let topicsApiUrl: string;

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
    await TestBed.configureTestingModule({
      imports: [ProfileComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    profileApiUrl = `${environment.apiUrl}/profile`;
    topicsApiUrl = `${environment.apiUrl}/topics/subscribed`;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should load user, prefill form and render fields on success', () => {
    fixture.detectChanges();

    const reqProfile = httpMock.expectOne(profileApiUrl);
    const reqTopics = httpMock.expectOne(topicsApiUrl);

    expect(reqProfile.request.method).toBe('GET');
    expect(reqTopics.request.method).toBe('GET');

    reqProfile.flush(mockUser);
    reqTopics.flush(mockTopics);
    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.error()).toBeNull();

    expect(component.form.value.email).toBe('john.doe@example.com');
    expect(component.form.value.username).toBe('jeanbiche');

    const usernameInput = debugElement.query(By.css('[data-testid="username-input"]'));
    const emailInput = debugElement.query(By.css('[data-testid="email-input"]'));
    const submitButton = debugElement.query(By.css('[data-testid="submit-button"]'));

    expect(usernameInput).toBeTruthy();
    expect(emailInput).toBeTruthy();
    expect(submitButton).toBeTruthy();
  });

  it('should display error message when getUser fails', () => {
    fixture.detectChanges();

    const reqProfile = httpMock.expectOne(profileApiUrl);
    const reqTopics = httpMock.expectOne(topicsApiUrl);

    expect(reqProfile.request.method).toBe('GET');
    expect(reqTopics.request.method).toBe('GET');

    reqProfile.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
    reqTopics.flush(mockTopics);
    fixture.detectChanges();

    expect(component.loading()).toBe(false);

    const errorElement = debugElement.query(By.css('[data-testid="error-screen"]'));
    expect(errorElement).toBeTruthy();
  });

  it('should display error message when getSubscribedTopics fails', () => {
    fixture.detectChanges();

    const reqProfile = httpMock.expectOne(profileApiUrl);
    const reqTopics = httpMock.expectOne(topicsApiUrl);

    reqProfile.flush(mockUser);
    reqTopics.flush(
      { message: 'Server error' },
      { status: 500, statusText: 'Internal Server Error' },
    );
    fixture.detectChanges();

    expect(component.loading()).toBe(false);

    const errorElement = debugElement.query(By.css('[data-testid="error-screen"]'));
    expect(errorElement).toBeTruthy();
  });

  it('should call updateUser on submit and refresh the form on success', () => {
    fixture.detectChanges();

    const reqProfile = httpMock.expectOne(profileApiUrl);
    const reqTopics = httpMock.expectOne(topicsApiUrl);

    reqProfile.flush(mockUser);
    reqTopics.flush(mockTopics);
    fixture.detectChanges();

    component.form.setValue({
      email: 'new.john@example.com',
      username: 'newjeanbiche',
      password: 'NewPassword123!',
    });

    fixture.detectChanges();

    const submitButton = debugElement.query(By.css('[data-testid="submit-button"]'));
    submitButton.nativeElement.click();

    const reqPut = httpMock.expectOne(profileApiUrl);
    expect(reqPut.request.method).toBe('PUT');
    expect(reqPut.request.body).toEqual({
      email: 'new.john@example.com',
      username: 'newjeanbiche',
      password: 'NewPassword123!',
    });

    const updatedUser: User = {
      ...mockUser,
      email: 'new.john@example.com',
      username: 'newjeanbiche',
      updatedAt: '2026-09-02T10:00:00Z',
    };

    reqPut.flush(updatedUser);
    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.user()).toEqual(updatedUser);
  });

  it('should unsubscribe from a topic and remove it from the rendered list', () => {
    fixture.detectChanges();

    const reqProfile = httpMock.expectOne(profileApiUrl);
    const reqTopics = httpMock.expectOne(topicsApiUrl);
    reqProfile.flush(mockUser);
    reqTopics.flush(mockTopics);
    fixture.detectChanges();

    const unsubscribeButtons = debugElement.queryAll(By.css('[data-testid="unsubscribe-button"]'));

    expect(unsubscribeButtons).toHaveLength(2);
    unsubscribeButtons[0].nativeElement.click();

    const req = httpMock.expectOne(`${environment.apiUrl}/topics/1/subscribe`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
    fixture.detectChanges();

    const updatedUnsubscribeButtons = debugElement.queryAll(
      By.css('[data-testid="unsubscribe-button"]'),
    );
    expect(component.topics()).toEqual([mockTopics[1]]);
    expect(updatedUnsubscribeButtons).toHaveLength(1);
    expect(fixture.nativeElement.textContent).not.toContain('Java');
  });

  it('should keep loading state consistent when updateUser fails', () => {
    fixture.detectChanges();

    const reqProfile = httpMock.expectOne(profileApiUrl);
    const reqTopics = httpMock.expectOne(topicsApiUrl);

    reqProfile.flush(mockUser);
    reqTopics.flush(mockTopics);
    fixture.detectChanges();

    component.form.setValue({
      email: 'new.john@example.com',
      username: 'newjeanbiche',
      password: 'NewPassword123!',
    });

    fixture.detectChanges();

    const submitButton = debugElement.query(By.css('[data-testid="submit-button"]'));
    submitButton.nativeElement.click();

    const reqPut = httpMock.expectOne(profileApiUrl);
    reqPut.flush({ message: 'Email is already in use' }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.user()).toEqual(mockUser);
  });
});
