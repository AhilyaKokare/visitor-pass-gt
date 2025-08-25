import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { VisitorPass } from '../models/pass.model';
import { Page } from '../models/page.model';

@Injectable({
  providedIn: 'root'
})
export class SecurityService {
  private getApiUrl(tenantId: number) {
    return `${environment.apiUrl}/tenants/${tenantId}/security`;
  }

  constructor(private http: HttpClient) { }

  // UPDATED to be paginated
  getTodaysDashboard(tenantId: number, page: number, size: number): Observable<Page<VisitorPass>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Page<VisitorPass>>(`${this.getApiUrl(tenantId)}/dashboard`, { params });
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