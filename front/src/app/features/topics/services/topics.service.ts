import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { TopicItem, TopicOption } from '../models';

@Injectable({ providedIn: 'root' })
export class TopicsService {
  private readonly httpClient = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/topics`;

  /**
   * Get all topics with a flag indicating whether the user is subscribed.
   * @returns An Observable emitting an array of TopicItem.
   */
  getAllTopics(): Observable<TopicItem[]> {
    return this.httpClient.get<TopicItem[]>(this.apiUrl);
  }

  /**
   * Get all topics as id/name pairs, used for selection fields (e.g. post form).
   * @returns An Observable emitting an array of TopicOption.
   */
  getTopicOptions(): Observable<TopicOption[]> {
    return this.httpClient.get<TopicOption[]>(`${this.apiUrl}/options`);
  }

  /**
   * Get all topics the user is currently subscribed to.
   * @returns An Observable emitting an array of subscribed TopicItem.
   */
  getSubscribedTopics(): Observable<TopicItem[]> {
    return this.httpClient.get<TopicItem[]>(`${this.apiUrl}/subscribed`);
  }

  /**
   * Subscribes the user to the topic identified by its id.
   * @param topicId - ID of the topic to subscribe to.
   * @returns An Observable emitting void when the subscription is complete.
   */
  subscribe(topicId: number): Observable<void> {
    return this.httpClient.post<void>(`${this.apiUrl}/${topicId}/subscribe`, {});
  }

  /**
   * Unsubscribes the user from the topic identified by its id.
   * @param topicId - ID of the topic to unsubscribe from.
   * @returns An Observable emitting void when the unsubscription is complete.
   */
  unsubscribe(topicId: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.apiUrl}/${topicId}/subscribe`);
  }
}
