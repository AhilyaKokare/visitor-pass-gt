import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { SecurityService, SecurityDashboardPage } from '../../../core/services/security.service';
import { AuthService } from '../../../core/services/auth.service';
import { VisitorPass } from '../../../core/models/pass.model';
import { LoadingSpinnerComponent } from '../../../shared/loading-spinner/loading-spinner.component';
import { ConfirmationService } from '../../../core/services/confirmation.service';

@Component({
  selector: 'app-security-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingSpinnerComponent, PaginationComponent, DatePipe],
  templateUrl: './security-dashboard.component.html',
})
export class SecurityDashboardComponent implements OnInit {
  approvedVisitors: SecurityPassInfo[] = [];
  checkedInVisitors: SecurityPassInfo[] = [];

  searchPassCode: string = '';
  searchedPass: VisitorPass | null = null;
  isLoading = true;
  isSearching = false;
  tenantId!: number;
  approvedCurrentPage = 0;
  onSiteCurrentPage = 0;
  pageSize = 5;
  private passChangeSubscription?: Subscription;

  // 5. Property to hold our WebSocket subscription
  private dashboardUpdateSubscription!: Subscription;

  // 6. Inject WebSocketService
  constructor(
    private securityService: SecurityService,
    private authService: AuthService,
    private toastr: ToastrService,
    private confirmationService: ConfirmationService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getDecodedToken();
    if (user && user.tenantId) {
      this.tenantId = user.tenantId;
      this.loadDashboard();
    }
  }

  loadDashboard(): void {
    this.isLoading = true;
    this.securityService.getTodaysDashboard(this.tenantId).subscribe({
      next: (data) => {
        this.approvedVisitors = data.filter((v) => v.status === 'APPROVED');
        this.checkedInVisitors = data.filter((v) => v.status === 'CHECKED_IN');
        this.isLoading = false;
      },
      error: () => {
        this.toastr.error('Failed to load dashboard data.');
        this.isLoading = false;
      },
    });
  }

  onSearch(): void {
    if (!this.searchPassCode.trim()) { this.searchedPass = null; return; }
    this.isSearching = true;
    this.securityService.searchPassByCode(this.tenantId, this.searchPassCode).subscribe({
      next: (data) => {
        this.searchedPass = data;
        this.isSearching = false;
      },
      error: () => {
        this.toastr.error(`No pass found with code: ${this.searchPassCode}`);
        this.searchedPass = null;
        this.isSearching = false;
      },
    });
  }

  checkIn(passId: number): void {
    if (this.confirmationService.confirm('Are you sure you want to check-in this visitor?')) {
      this.securityService.checkIn(this.tenantId, passId).subscribe({
        next: () => {
          this.toastr.success('Visitor checked in successfully.');
          this.refreshDataAfterAction();
        },
        error: (err) => this.toastr.error(err.error.message || 'Failed to check-in visitor.'),
      });
    }
  }

  checkOut(passId: number): void {
    if (this.confirmationService.confirm('Are you sure you want to check-out this visitor?')) {
      this.securityService.checkOut(this.tenantId, passId).subscribe({
        next: () => {
          this.toastr.info('Visitor checked out successfully.');
          this.refreshDataAfterAction();
        },
        error: (err) => this.toastr.error(err.error.message || 'Failed to check-out visitor.'),
      });
    }
  }

  refreshDataAfterAction(): void {
    this.loadDashboard();
    if (this.searchedPass) {
      this.searchPassCode = this.searchedPass.passCode;
      this.onSearch();
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'APPROVED': return 'bg-primary';
      case 'CHECKED_IN': return 'bg-info text-dark';
      case 'CHECKED_OUT': return 'bg-success';
      case 'REJECTED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }
}
