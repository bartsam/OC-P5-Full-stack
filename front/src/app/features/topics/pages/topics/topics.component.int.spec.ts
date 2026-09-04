import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../../../../environments/environment';
import { TopicItem } from '../../models';
import { TopicsComponent } from './topics.component';

describe('TopicsComponent integration', () => {
  let component: TopicsComponent;
  let fixture: ComponentFixture<TopicsComponent>;
  let httpMock: HttpTestingController;

  const topicsApiUrl = `${environment.apiUrl}/topics`;
  const mockTopics: TopicItem[] = [
    { id: 1, name: 'Java', description: 'Backend', isSubscribed: false },
    { id: 2, name: 'Angular', description: 'Frontend', isSubscribed: true },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TopicsComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(TopicsComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should subscribe from the topic item and disable its button after the request succeeds', () => {
    fixture.detectChanges();

    const getTopicsRequest = httpMock.expectOne(topicsApiUrl);
    expect(getTopicsRequest.request.method).toBe('GET');
    getTopicsRequest.flush(mockTopics);
    fixture.detectChanges();

    const subscribeButton = fixture.nativeElement.querySelector(
      '[data-testid="subscribe-button"]',
    ) as HTMLButtonElement;

    expect(subscribeButton.disabled).toBe(false);
    expect(subscribeButton.textContent).toContain("S'abonner");

    subscribeButton.click();

    const subscribeRequest = httpMock.expectOne(`${topicsApiUrl}/1/subscribe`);
    expect(subscribeRequest.request.method).toBe('POST');
    expect(subscribeRequest.request.body).toEqual({});
    subscribeRequest.flush(null);
    fixture.detectChanges();

    expect(component.topics()[0].isSubscribed).toBe(true);

    const updatedSubscribeButton = fixture.nativeElement.querySelector(
      '[data-testid="subscribe-button"]',
    ) as HTMLButtonElement;

    expect(updatedSubscribeButton.disabled).toBe(true);
    expect(updatedSubscribeButton.textContent).toContain('Déjà abonné');
  });
});
