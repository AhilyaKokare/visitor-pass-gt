import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { VisitorPass } from '../models/pass.model';
import { Page } from '../models/page.model';

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

  getMyPassHistory(tenantId: number, page: number, size: number): Observable<Page<VisitorPass>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Page<VisitorPass>>(`${this.getApiUrl(tenantId)}/passes/history`, { params });
  }

  // **** THIS IS THE MISSING METHOD THAT IS NOW ADDED ****
  /**
   * Fetches a paginated list of all visitor passes for a given tenant.
   * This is used by Tenant Admins and Approvers.
   * @param tenantId The ID of the tenant.
   * @param page The page number to retrieve (0-indexed).
   * @param size The number of items per page.
   * @returns An Observable of a Page of VisitorPass objects.
   */
  getPassesForTenant(tenantId: number, page: number, size: number): Observable<Page<VisitorPass>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    // This points to the correct endpoint for fetching all passes in a tenant
    return this.http.get<Page<VisitorPass>>(`${this.getApiUrl(tenantId)}/passes`, { params });
  }

  // This method can be kept for components that only need a non-paginated list of pending passes,
  // though using the paginated version is generally better for scalability.
  getPendingPasses(tenantId: number): Observable<VisitorPass[]> {
    return this.http.get<Page<VisitorPass>>(`${this.getApiUrl(tenantId)}/passes`).pipe(
        map(response => response.content.filter(pass => pass.status === 'PENDING'))
      );
  }

  approvePass(tenantId: number, passId: number): Observable<any> {
    return this.http.post(`${this.getApiUrl(tenantId)}/approvals/${passId}/approve`, {});
  }

  rejectPass(tenantId: number, passId: number, reason: string): Observable<any> {
    return this.http.post(`${this.getApiUrl(tenantId)}/approvals/${passId}/reject`, { reason });
  }
}
