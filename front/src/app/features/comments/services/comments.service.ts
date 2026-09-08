import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { CommentCreateRequest, CommentItem } from '../models';

@Injectable({ providedIn: 'root' })
export class CommentsService {
  private readonly httpClient = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/posts`;

  /**
   * Creates a new comment for a post.
   *
   * @param content - The text content of the comment.
   * @param postId - The unique identifier of the target post.
   * @returns An {@link Observable} emitting the newly created {@link CommentItem}.
   */
  createComment(request: CommentCreateRequest, postId: number): Observable<CommentItem> {
    return this.httpClient.post<CommentItem>(`${this.apiUrl}/${postId}/comments`, request);
  }

  /**
   * Retrieves all comments associated with a specific post.
   *
   * @param postId - The unique identifier of the target post.
   * @returns An {@link Observable} emitting an array of {@link CommentItem} objects.
   */
  getComments(postId: number): Observable<CommentItem[]> {
    return this.httpClient.get<CommentItem[]>(`${this.apiUrl}/${postId}/comments`, {});
  }
}
