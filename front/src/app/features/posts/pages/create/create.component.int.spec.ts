import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DebugElement } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { NotificationService } from '@shared/services/notification.service';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { environment } from '../../../../../environments/environment';
import { TopicOption } from '../../../topics/models';
import { PostCreateComponent } from './create.component';

describe('PostCreateComponent integration', () => {
  let component: PostCreateComponent;
  let fixture: ComponentFixture<PostCreateComponent>;
  let debugElement: DebugElement;
  let httpMock: HttpTestingController;
  let mockRouter: { navigate: ReturnType<typeof vi.fn> };
  let mockNotificationService: {
    success: ReturnType<typeof vi.fn>;
    error: ReturnType<typeof vi.fn>;
  };

  const topicsUrl = `${environment.apiUrl}/topics/options`;
  const postsUrl = `${environment.apiUrl}/posts`;

  const mockTopics: TopicOption[] = [{ id: 1, name: 'Java' }];

  beforeEach(async () => {
    mockRouter = { navigate: vi.fn() };
    mockNotificationService = { success: vi.fn(), error: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [PostCreateComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: Router, useValue: mockRouter },
        { provide: NotificationService, useValue: mockNotificationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PostCreateComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should display the spinner, then render the form after topics load succeeds', () => {
    fixture.detectChanges();

    expect(debugElement.query(By.css('[data-testid="loading-screen"]'))).toBeTruthy();

    httpMock.expectOne(topicsUrl).flush(mockTopics);
    fixture.detectChanges();

    expect(debugElement.query(By.css('[data-testid="loading-screen"]'))).toBeNull();
    expect(debugElement.query(By.css('[data-testid="submit-button"]'))).toBeTruthy();
  });

  it('should display the error screen when topics loading fails', () => {
    fixture.detectChanges();

    httpMock
      .expectOne(topicsUrl)
      .flush({ message: 'Server error' }, { status: 500, statusText: 'Internal Server Error' });
    fixture.detectChanges();

    const errorScreen = debugElement.query(By.css('[data-testid="error-screen"]'));
    expect(errorScreen.nativeElement.textContent).toContain('Impossible de charger les topics');
  });

  it('should create the post, notify success and navigate to feed on submit', () => {
    fixture.detectChanges();
    httpMock.expectOne(topicsUrl).flush(mockTopics);
    fixture.detectChanges();

    component.form.setValue({ topicId: 1, title: 'Titre valide', content: 'Contenu valide' });
    fixture.detectChanges();

    const submitButton = debugElement.query(By.css('[data-testid="submit-button"]'));
    submitButton.nativeElement.click();

    const createReq = httpMock.expectOne(postsUrl);
    expect(createReq.request.method).toBe('POST');
    createReq.flush(null);

    expect(mockNotificationService.success).toHaveBeenCalledWith('Article créé avec succès.');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/posts/feed']);
  });

  it('should notify an error and keep the form when create fails', () => {
    fixture.detectChanges();
    httpMock.expectOne(topicsUrl).flush(mockTopics);
    fixture.detectChanges();

    component.form.setValue({ topicId: 1, title: 'Titre valide', content: 'Contenu valide' });
    fixture.detectChanges();

    const submitButton = debugElement.query(By.css('[data-testid="submit-button"]'));
    submitButton.nativeElement.click();

    const createReq = httpMock.expectOne(postsUrl);
    createReq.flush({ message: 'Forbidden' }, { status: 403, statusText: 'Forbidden' });

    expect(mockNotificationService.error).toHaveBeenCalledWith(
      "Impossible de créer l'article : Forbidden",
    );
    expect(component.loading()).toBe(false);
  });
});
