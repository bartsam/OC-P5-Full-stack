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

  getUser(): Observable<User> {
    return this.httpClient.get<User>(`${this.apiUrl}/profile`);
  }

  updateUser(request: UserUpdateRequest): Observable<User> {
    return this.httpClient.put<User>(`${this.apiUrl}/profile`, request);
  }
}
