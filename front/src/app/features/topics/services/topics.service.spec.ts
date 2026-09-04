import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { fail } from 'assert';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { TopicItem, TopicOption } from '../models';
import { TopicsService } from './topics.service';

describe('TopicsService', () => {
  let service: TopicsService;
  let httpMock: HttpTestingController;
  let apiUrl: string;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TopicsService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(TopicsService);
    httpMock = TestBed.inject(HttpTestingController);

    apiUrl = service['apiUrl'];
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAllTopics', () => {
    it('should call GET /api/topics and return an array of TopicItem', () => {
      const mockTopics: TopicItem[] = [
        { id: 1, name: 'Java', description: 'Java ecosystem', isSubscribed: true },
        { id: 2, name: 'Angular', description: 'Angular framework', isSubscribed: false },
      ];

      service.getAllTopics().subscribe(topics => {
        expect(topics).toEqual(mockTopics);
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('GET');

      req.flush(mockTopics);
    });

    it('should propagate error when getAllTopics returns 401', () => {
      service.getAllTopics().subscribe({
        next: () => fail('expected error'),
        error: error => {
          expect(error.status).toBe(401);
        },
      });

      const req = httpMock.expectOne(apiUrl);
      req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('getTopicOptions', () => {
    it('should call GET /api/topics/options and return an array of TopicOption', () => {
      const mockOptions: TopicOption[] = [
        { id: 1, name: 'Java' },
        { id: 2, name: 'Angular' },
      ];

      service.getTopicOptions().subscribe(options => {
        expect(options).toEqual(mockOptions);
      });

      const req = httpMock.expectOne(`${apiUrl}/options`);
      expect(req.request.method).toBe('GET');

      req.flush(mockOptions);
    });

    it('should propagate error when getTopicOptions returns 401', () => {
      service.getTopicOptions().subscribe({
        next: () => fail('expected error'),
        error: error => {
          expect(error.status).toBe(401);
        },
      });

      const req = httpMock.expectOne(`${apiUrl}/options`);
      req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('getSubscribedTopics', () => {
    it('should call GET /api/topics/subscribed and return only subscribed TopicItem', () => {
      const mockSubscribed: TopicItem[] = [
        { id: 1, name: 'Java', description: 'Java ecosystem', isSubscribed: true },
      ];

      service.getSubscribedTopics().subscribe(topics => {
        expect(topics).toEqual(mockSubscribed);
      });

      const req = httpMock.expectOne(`${apiUrl}/subscribed`);
      expect(req.request.method).toBe('GET');

      req.flush(mockSubscribed);
    });

    it('should propagate error when getSubscribedTopics returns 401', () => {
      service.getSubscribedTopics().subscribe({
        next: () => fail('expected error'),
        error: error => {
          expect(error.status).toBe(401);
        },
      });

      const req = httpMock.expectOne(`${apiUrl}/subscribed`);
      req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('subscribe', () => {
    it('should call POST /api/topics/{id}/subscribe', () => {
      const topicId = 1;

      service.subscribeTopic(topicId).subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne(`${apiUrl}/${topicId}/subscribe`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({});

      req.flush(null, { status: 204, statusText: 'No Content' });
    });

    it('should propagate error when subscribe returns 404', () => {
      const topicId = 999;

      service.subscribeTopic(topicId).subscribe({
        next: () => fail('expected error'),
        error: error => {
          expect(error.status).toBe(404);
        },
      });

      const req = httpMock.expectOne(`${apiUrl}/${topicId}/subscribe`);
      req.flush({ message: 'Topic not found' }, { status: 404, statusText: 'Not Found' });
    });
  });

  describe('unsubscribe', () => {
    it('should call DELETE /api/topics/{id}/subscribe', () => {
      const topicId = 1;

      service.unSubscribeTopic(topicId).subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne(`${apiUrl}/${topicId}/subscribe`);
      expect(req.request.method).toBe('DELETE');

      req.flush(null, { status: 204, statusText: 'No Content' });
    });

    it('should propagate error when unsubscribe returns 404', () => {
      const topicId = 999;

      service.unSubscribeTopic(topicId).subscribe({
        next: () => fail('expected error'),
        error: error => {
          expect(error.status).toBe(404);
        },
      });

      const req = httpMock.expectOne(`${apiUrl}/${topicId}/subscribe`);
      req.flush({ message: 'Topic not found' }, { status: 404, statusText: 'Not Found' });
    });
  });
});
