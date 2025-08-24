import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SecurityPassInfo, VisitorPass } from '../models/pass.model';
import { Page } from '../models/page.model'; // Assuming you have this from previous steps

// --- THIS IS THE FIX for the "no exported member" error ---
// This interface defines the shape of the paginated response from the backend.
export interface SecurityDashboardPage {
  approvedForEntry: Page<SecurityPassInfo>;
  currentlyOnSite: Page<SecurityPassInfo>;
}

@Injectable({
  providedIn: 'root'
})
export class SecurityService {
  private getApiUrl(tenantId: number) {
    return `${environment.apiUrl}/tenants/${tenantId}/security`;
  }

  constructor(private http: HttpClient) { }

  // --- THIS IS THE FIX for the "Expected 1 arguments, but got 4" error ---
  // The method signature is updated to accept all necessary pagination parameters.
  getTodaysDashboard(tenantId: number, approvedPage: number, onSitePage: number, size: number): Observable<SecurityDashboardPage> {
    // Spring Boot's @Qualifier expects parameters to be prefixed.
    // e.g., 'approved_page', 'onSite_page'
    const params = new HttpParams()
      .set('approved_page', approvedPage.toString())
      .set('approved_size', size.toString())
      .set('onSite_page', onSitePage.toString())
      .set('onSite_size', size.toString());

    return this.http.get<SecurityDashboardPage>(`${this.getApiUrl(tenantId)}/dashboard/today`, { params });
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
