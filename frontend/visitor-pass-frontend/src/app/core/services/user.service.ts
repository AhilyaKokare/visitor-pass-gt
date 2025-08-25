// FILE: frontend/visitor-pass-frontend/src/app/core/services/user.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User } from '../models/user.model';
import { Page } from '../models/page.model'; // <-- Ensure this import is correct

@Injectable({
  providedIn: 'root'
})
export class UserService {
  // --- ADDED a public http property for your debug methods ---
  public readonly http: HttpClient;

  private getApiUrl(tenantId: number) {
    return `${environment.apiUrl}/tenants/${tenantId}/admin`;
  }

  constructor(httpClient: HttpClient) {
    this.http = httpClient;
   }

  // VVV --- THIS IS THE CORRECTED METHOD --- VVV
  getUsers(tenantId: number, page: number, size: number): Observable<Page<User>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    // This now correctly expects a Page<User> object from the backend
    return this.http.get<Page<User>>(`${this.getApiUrl(tenantId)}/users`, { params });
  }

  createUser(tenantId: number, userData: any): Observable<User> {
    return this.http.post<User>(`${this.getApiUrl(tenantId)}/users`, userData);
  }

  updateUserStatus(tenantId: number, userId: number, isActive: boolean): Observable<User> {
    return this.http.put<User>(`${this.getApiUrl(tenantId)}/users/${userId}/status`, { isActive });
  }
}