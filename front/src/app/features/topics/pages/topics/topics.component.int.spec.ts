import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DebugElement } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { environment } from '../../../../../environments/environment';
import { NotificationService } from '../../../../shared/services/notification.service';
import { TopicItem } from '../../models';
import { TopicsComponent } from './topics.component';

describe('TopicsComponent integration', () => {
  let component: TopicsComponent;
  let debugElement: DebugElement;
  let fixture: ComponentFixture<TopicsComponent>;
  let httpMock: HttpTestingController;
  let mockNotificationService: { error: ReturnType<typeof vi.fn> };

  const apiUrl = `${environment.apiUrl}/topics`;
  const mockTopics: TopicItem[] = [
    { id: 1, name: 'Java', description: 'Backend', isSubscribed: false },
    { id: 2, name: 'Angular', description: 'Frontend', isSubscribed: true },
  ];

  beforeEach(async () => {
    mockNotificationService = { error: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [TopicsComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: NotificationService, useValue: mockNotificationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TopicsComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should subscribe from the topic item and disable its button after the request succeeds', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mockTopics);
    fixture.detectChanges();

    const subscribeButton = debugElement.query(By.css('[data-testid="subscribe-button"]'));
    expect(subscribeButton.nativeElement.disabled).toBe(false);
    expect(subscribeButton.nativeElement.textContent).toContain("S'abonner");

    subscribeButton.nativeElement.click();

    const subscribeRequest = httpMock.expectOne(`${apiUrl}/1/subscribe`);
    expect(subscribeRequest.request.method).toBe('POST');
    expect(subscribeRequest.request.body).toEqual({});
    subscribeRequest.flush(null);
    fixture.detectChanges();

    expect(component.topics()[0].isSubscribed).toBe(true);

    const updatedSubscribeButton = debugElement.query(By.css('[data-testid="subscribe-button"]'));
    expect(updatedSubscribeButton.nativeElement.disabled).toBe(true);
    expect(updatedSubscribeButton.nativeElement.textContent).toContain('Déjà abonné');
  });

  it('should display the loading error returned by GET /topics', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne(apiUrl);
    req.flush({ message: 'Server error' }, { status: 500, statusText: 'Internal Server Error' });
    fixture.detectChanges();

    const errorScreen = debugElement.query(By.css('[data-testid="error-screen"]'));
    expect(errorScreen.nativeElement.textContent).toContain('Impossible de charger les topics');
    expect(debugElement.query(By.css('[data-testid="topics-list"]'))).toBeNull();
  });

  it('should keep the button enabled and notify when POST /subscribe fails', () => {
    fixture.detectChanges();

    httpMock.expectOne(apiUrl).flush(mockTopics);
    fixture.detectChanges();

    const subscribeButton = debugElement.query(By.css('[data-testid="subscribe-button"]'));
    subscribeButton.nativeElement.click();

    const subscribeRequest = httpMock.expectOne(`${apiUrl}/1/subscribe`);
    subscribeRequest.flush({ message: 'Forbidden' }, { status: 403, statusText: 'Forbidden' });
    fixture.detectChanges();

    expect(subscribeButton.nativeElement.disabled).toBe(false);
    expect(mockNotificationService.error).toHaveBeenCalledWith(
      "Impossible de s'abonner à ce thème : Forbidden",
    );
  });
});
