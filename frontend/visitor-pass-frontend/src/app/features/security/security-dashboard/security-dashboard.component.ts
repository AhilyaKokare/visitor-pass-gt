import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { SecurityService, SecurityDashboardPage } from '../../../core/services/security.service';
import { AuthService } from '../../../core/services/auth.service';
import { VisitorPass, SecurityPassInfo } from '../../../core/models/pass.model';
import { LoadingSpinnerComponent } from '../../../shared/loading-spinner/loading-spinner.component';
import { ConfirmationService } from '../../../core/services/confirmation.service';
import { Page, PaginationComponent } from '../../../shared/pagination/pagination.component';
import { StateService } from '../../../core/services/state.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-security-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingSpinnerComponent, PaginationComponent, DatePipe],
  templateUrl: './security-dashboard.component.html',
})
export class SecurityDashboardComponent implements OnInit, OnDestroy {

  approvedVisitorsPage: Page<SecurityPassInfo> | null = null;
  checkedInVisitorsPage: Page<SecurityPassInfo> | null = null;
  searchPassCode: string = '';
  searchedPass: VisitorPass | null = null;
  isLoading = true;
  isSearching = false;
  tenantId!: number;
  approvedCurrentPage = 0;
  onSiteCurrentPage = 0;
  pageSize = 5;
  private passChangeSubscription?: Subscription;

  constructor(
    private securityService: SecurityService,
    private authService: AuthService,
    private toastr: ToastrService,
    private confirmationService: ConfirmationService,
    private stateService: StateService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getDecodedToken();
    if (user && user.tenantId) {
      this.tenantId = user.tenantId;
      this.loadDashboard();

      this.passChangeSubscription = this.stateService.passChange$.subscribe(() => {
        console.log('SecurityDashboardComponent received notification. Resetting pages and refreshing.');
        
        // --- THIS IS THE CRITICAL FIX ---
        // When a change happens elsewhere, reset this dashboard's pages to 0
        // to ensure new items (like a newly approved pass) are visible.
        this.approvedCurrentPage = 0;
        this.onSiteCurrentPage = 0;
        this.loadDashboard();
      });
    }
  }

  ngOnDestroy(): void {
    this.passChangeSubscription?.unsubscribe();
  }

  loadDashboard(): void {
    this.isLoading = true;
    this.securityService.getTodaysDashboard(this.tenantId, this.approvedCurrentPage, this.onSiteCurrentPage, this.pageSize).subscribe({
      next: (data: SecurityDashboardPage) => {
        this.approvedVisitorsPage = data.approvedForEntry;
        this.checkedInVisitorsPage = data.currentlyOnSite;
        this.isLoading = false;
      },
      error: () => {
        this.toastr.error('Failed to load dashboard data.');
        this.isLoading = false;
      },
    });
  }

  onApprovedPageChange(pageNumber: number): void {
    this.approvedCurrentPage = pageNumber;
    this.loadDashboard();
  }

  onOnSitePageChange(pageNumber: number): void {
    this.onSiteCurrentPage = pageNumber;
    this.loadDashboard();
  }

  checkIn(passId: number): void {
    if (this.confirmationService.confirm('Are you sure you want to check-in this visitor?')) {
      this.securityService.checkIn(this.tenantId, passId).subscribe({
        next: () => {
          this.toastr.success('Visitor checked in successfully.');
          this.stateService.notifyPassChange();
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
          this.stateService.notifyPassChange();
        },
        error: (err) => this.toastr.error(err.error.message || 'Failed to check-out visitor.'),
      });
    }
  }

  // --- Search functionality remains the same ---
  onSearch(): void {
    if (!this.searchPassCode.trim()) {
      this.searchedPass = null;
      return;
    }
    this.isSearching = true;
    this.securityService.searchPassByCode(this.tenantId, this.searchPassCode).subscribe({
      next: (data) => {
        this.searchedPass = data;
        this.toastr.success(`Found pass for ${data.visitorName}`);
        this.isSearching = false;
      },
      error: () => {
        this.toastr.error(`No pass found with code: ${this.searchPassCode}`);
        this.searchedPass = null;
        this.isSearching = false;
      },
    });
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