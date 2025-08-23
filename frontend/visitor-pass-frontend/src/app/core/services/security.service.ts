import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SecurityPassInfo, VisitorPass } from '../models/pass.model';

@Injectable({
  providedIn: 'root'
})
export class SecurityService {
  private getApiUrl(tenantId: number) {
    return `${environment.apiUrl}/tenants/${tenantId}/security`;
  }

  constructor(private http: HttpClient) { }

  getTodaysDashboard(tenantId: number): Observable<SecurityPassInfo[]> {
    return this.http.get<SecurityPassInfo[]>(`${this.getApiUrl(tenantId)}/dashboard/today`);
  }

  searchPassByCode(tenantId: number, passCode: string): Observable<VisitorPass> {
    return this.http.get<VisitorPass>(`${this.getApiUrl(tenantId)}/passes/search`, { params: { passCode } });
  }

  checkIn(tenantId: number, passId: number): Observable<any> {
    return this.http.post(`${this.getApiUrl(tenantId)}/check-in/${passId}`, {});
  }

  checkOut(tenantId: number, passId: number): Observable<any> {
    return this.http.post(`${this.getApiUrl(tenantId)}/check-out/${passId}`, {});
  }
}
