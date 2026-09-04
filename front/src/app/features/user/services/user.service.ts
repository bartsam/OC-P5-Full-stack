import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { User } from '../models';
import { UserUpdateRequest } from '../models/update.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly httpClient = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  /**
   * Fetches the profile details of the authenticated user.
   * @returns An Observable emitting the current User profile.
   */
  getUser(): Observable<User> {
    return this.httpClient.get<User>(`${this.apiUrl}/profile`);
  }

  /**
   * Updates the profile information of the authenticated user.
   * @param request - The updated user details (email, username, password).
   * @returns An Observable emitting the updated User profile.
   */
  updateUser(request: UserUpdateRequest): Observable<User> {
    return this.httpClient.put<User>(`${this.apiUrl}/profile`, request);
  }
}
