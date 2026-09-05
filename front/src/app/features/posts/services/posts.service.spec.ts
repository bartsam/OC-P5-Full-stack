import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { provideHttpClient } from '@angular/common/http';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { PostCreateRequest, PostDetail, PostItem } from '../models';
import { PostsService } from './posts.service';

describe('PostsService', () => {
  let service: PostsService;
  let httpMock: HttpTestingController;
  let apiUrl: string;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PostsService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(PostsService);
    httpMock = TestBed.inject(HttpTestingController);

    apiUrl = service['apiUrl'];
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('createPost', () => {
    it('should call POST /posts with the correct payload and return created post', () => {
      // GIVEN
      const request: PostCreateRequest = {
        title: 'Découvrir Spring Boot',
        content: 'Spring Boot est un framework...',
        topicId: 1,
      };

      const expectedResponse: PostDetail = {
        id: 10,
        author: 'john',
        topic: 'Java',
        title: 'Découvrir Spring Boot',
        content: 'Spring Boot est un framework...',
        createdAt: '2025-01-01T10:00:00',
      };

      // WHEN
      service.createPost(request).subscribe(response => {
        expect(response).toEqual(expectedResponse);
      });

      // THEN
      const req = httpMock.expectOne(`${apiUrl}`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);

      req.flush(expectedResponse);
    });
  });

  describe('getPost', () => {
    it('should call GET /posts/:id and return post detail', () => {
      // GIVEN
      const postId = 10;

      const expectedResponse: PostDetail = {
        id: postId,
        author: 'john',
        topic: 'Java',
        title: 'Découvrir Spring Boot',
        content: 'Spring Boot est un framework...',
        createdAt: '2025-01-01T10:00:00',
      };

      // WHEN
      service.getPost(postId).subscribe(response => {
        expect(response).toEqual(expectedResponse);
      });

      // THEN
      const req = httpMock.expectOne(`${apiUrl}/${postId}`);
      expect(req.request.method).toBe('GET');

      req.flush(expectedResponse);
    });
  });

  describe('getFeed', () => {
    it('should call GET /posts and return list of post items', () => {
      // GIVEN
      const expectedResponse: PostItem[] = [
        {
          id: 2,
          title: 'New post',
          content: 'New content…',
          createdAt: '2025-01-02T10:00:00',
        },
        {
          id: 1,
          title: 'Old post',
          content: 'Old content…',
          createdAt: '2025-01-01T10:00:00',
        },
      ];

      // WHEN
      service.getFeed().subscribe(response => {
        expect(response).toEqual(expectedResponse);
      });

      // THEN
      const req = httpMock.expectOne(`${apiUrl}`);
      expect(req.request.method).toBe('GET');

      req.flush(expectedResponse);
    });
  });
});
