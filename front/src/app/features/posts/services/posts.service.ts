import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PostCreateRequest, PostDetail, PostItem } from '../models';

@Injectable({ providedIn: 'root' })
export class PostsService {
  private readonly httpClient = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/posts`;

  /**
   * Creates a new post.
   *
   * @param request - The payload containing the details required to create a post.
   * @returns An `Observable` emitting the detailed information of the newly created post.
   */
  createPost(request: PostCreateRequest): Observable<PostDetail> {
    return this.httpClient.post<PostDetail>(this.apiUrl, request);
  }

  /**
   * Retrieves the details of a specific post by its unique identifier.
   *
   * @param postId - The unique identifier of the post to retrieve.
   * @returns An `Observable` emitting the details of the requested post.
   */
  getPost(postId: number): Observable<PostDetail> {
    return this.httpClient.get<PostDetail>(`${this.apiUrl}/${postId}`);
  }

  /**
   * Fetches the list of posts for the feed.
   *
   * @returns An `Observable` emitting an array of post items representing the feed.
   */
  getFeed(): Observable<PostItem[]> {
    return this.httpClient.get<PostItem[]>(this.apiUrl);
  }
}
