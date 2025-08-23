import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { VisitorPass } from '../models/pass.model';
import { Page } from '../models/page.model'; // <-- This path should now be correct

@Injectable({
  providedIn: 'root'
})
export class PassService {
  private getApiUrl(tenantId: number) {
    return `${environment.apiUrl}/tenants/${tenantId}`;
  }

  constructor(private http: HttpClient) { }

  createPass(tenantId: number, passData: any): Observable<VisitorPass> {
    return this.http.post<VisitorPass>(`${this.getApiUrl(tenantId)}/passes`, passData);
  }

  // UPDATED METHOD
  getMyPassHistory(tenantId: number, page: number, size: number): Observable<Page<VisitorPass>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Page<VisitorPass>>(`${this.getApiUrl(tenantId)}/passes/history`, { params });
  }

  getPendingPasses(tenantId: number): Observable<VisitorPass[]> {
    return this.http.get<Page<VisitorPass>>(`${this.getApiUrl(tenantId)}/passes`).pipe(
        map(response => response.content)
      );
  }

  approvePass(tenantId: number, passId: number): Observable<any> {
    return this.http.post(`${this.getApiUrl(tenantId)}/approvals/${passId}/approve`, {});
  }

  rejectPass(tenantId: number, passId: number, reason: string): Observable<any> {
    return this.http.post(`${this.getApiUrl(tenantId)}/approvals/${passId}/reject`, { reason });
  }
}