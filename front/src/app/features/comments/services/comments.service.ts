import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { CommentCreateRequest, CommentItem } from '../models';

@Injectable({ providedIn: 'root' })
export class CommentsService {
  private readonly httpClient = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/comments`;

  /**
   * Creates a new comment for a post.
   *
   * @param request - The payload containing comment creation details.
   * @returns An {@link Observable} emitting the newly created {@link CommentItem}.
   */
  createComment(request: CommentCreateRequest): Observable<CommentItem> {
    return this.httpClient.post<CommentItem>(this.apiUrl, request);
  }

  /**
   * Retrieves all comments associated with a specific post.
   *
   * @param postId - The unique identifier of the target post.
   * @returns An {@link Observable} emitting an array of {@link CommentItem} objects.
   */
  getComments(postId: string): Observable<CommentItem[]> {
    return this.httpClient.get<CommentItem[]>(`${this.apiUrl}/${postId}/subscribe`, {});
  }
}
