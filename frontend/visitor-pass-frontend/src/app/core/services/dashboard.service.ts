import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TenantDashboardResponse } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private getApiUrl(tenantId: number) {
    return `${environment.apiUrl}/tenants/${tenantId}/admin`;
  }

  constructor(private http: HttpClient) { }

  getTenantDashboardData(tenantId: number): Observable<TenantDashboardResponse> {
    return this.http.get<TenantDashboardResponse>(`${this.getApiUrl(tenantId)}/dashboard`);
  }
}
