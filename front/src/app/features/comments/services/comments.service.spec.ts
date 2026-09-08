import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { provideHttpClient } from '@angular/common/http';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { CommentCreateRequest, CommentItem } from '../models';
import { CommentsService } from './comments.service';

describe('CommentsService', () => {
  let service: CommentsService;
  let httpMock: HttpTestingController;
  let apiUrl: string;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CommentsService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(CommentsService);
    httpMock = TestBed.inject(HttpTestingController);

    apiUrl = service['apiUrl'];
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('createComment', () => {
    it('should call POST /comments with the correct payload and return created comment', () => {
      // GIVEN
      const request: CommentCreateRequest = {
        userId: 1,
        postId: 1,
        content: 'Excellent article, merci pour le partage !',
      };

      const result: CommentItem = {
        id: 10,
        author: 'jeanbiche',
        content: 'Excellent article, merci pour le partage !',
        createdAt: '2026-09-08T12:00:00',
      };

      // WHEN
      service.createComment(request).subscribe(response => {
        expect(response).toEqual(result);
      });

      // THEN
      const req = httpMock.expectOne(`${apiUrl}`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);

      req.flush(result);
    });
  });

  describe('getComments', () => {
    it('should call GET /comments/:postId/subscribe and return list of comments', () => {
      // GIVEN
      const postId = '1';

      const result: CommentItem[] = [
        {
          id: 1,
          author: 'user1',
          content: 'Premier commentaire',
          createdAt: '2026-09-08T10:00:00',
        },
        {
          id: 2,
          author: 'user2',
          content: 'Deuxième commentaire',
          createdAt: '2026-09-08T11:00:00',
        },
      ];

      // WHEN
      service.getComments(postId).subscribe(response => {
        expect(response).toEqual(result);
      });

      // THEN
      const req = httpMock.expectOne(`${apiUrl}/${postId}/subscribe`);
      expect(req.request.method).toBe('GET');

      req.flush(result);
    });
  });
});
