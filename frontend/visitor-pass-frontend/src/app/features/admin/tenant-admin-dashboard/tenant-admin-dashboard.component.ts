import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../../core/services/auth.service';
import { DashboardService } from '../../../core/services/dashboard.service';
import { TenantDashboardResponse } from '../../../core/models/dashboard.model';
import { LoadingSpinnerComponent } from '../../../shared/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-tenant-admin-dashboard',
  standalone: true,
  imports: [CommonModule, DatePipe, LoadingSpinnerComponent],
  templateUrl: './tenant-admin-dashboard.component.html',
})
export class TenantAdminDashboardComponent implements OnInit {
  dashboardData: TenantDashboardResponse | null = null;
  tenantId!: number;
  isLoading = true;

  constructor(
    private dashboardService: DashboardService,
    private authService: AuthService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getDecodedToken();
    if (user && user.tenantId) {
      this.tenantId = user.tenantId;
      this.loadDashboard();
    } else {
      this.toastr.error('Could not determine tenant to load dashboard.');
      this.isLoading = false;
    }
  }

  loadDashboard(): void {
    this.isLoading = true;
    this.dashboardService.getTenantDashboardData(this.tenantId).subscribe({
      next: (data) => {
        this.dashboardData = data;
        this.isLoading = false;
      },
      error: () => {
        this.toastr.error('Failed to load tenant dashboard.');
        this.isLoading = false;
      },
    });
  }
}
