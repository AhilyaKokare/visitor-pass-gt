import { Component, OnInit, OnDestroy } from '@angular/core'; // 1. Add OnDestroy
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { SecurityService } from '../../../core/services/security.service';
import { AuthService } from '../../../core/services/auth.service';
import { VisitorPass } from '../../../core/models/pass.model';
import { LoadingSpinnerComponent } from '../../../shared/loading-spinner/loading-spinner.component';
import { ConfirmationService } from '../../../core/services/confirmation.service';
import { Page } from '../../../core/models/page.model';
import { PaginationComponent } from '../../../shared/pagination/pagination.component';
import { WebSocketService } from '../../../core/services/websocket.service'; // 2. Import WebSocketService
import { Subscription } from 'rxjs'; // 3. Import Subscription

@Component({
  selector: 'app-security-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingSpinnerComponent, PaginationComponent, DatePipe],
  templateUrl: './security-dashboard.component.html',
})
export class SecurityDashboardComponent implements OnInit, OnDestroy { // 4. Implement OnDestroy
  
  dashboardPage: Page<VisitorPass> | null = null;
  currentPage = 0;
  pageSize = 10;
  searchPassCode: string = '';
  searchedPass: VisitorPass | null = null;
  isLoading = true;
  isSearching = false;
  tenantId!: number;

  // 5. Property to hold our WebSocket subscription
  private dashboardUpdateSubscription!: Subscription;

  // 6. Inject WebSocketService
  constructor(
    private securityService: SecurityService,
    private authService: AuthService,
    private toastr: ToastrService,
    private confirmationService: ConfirmationService,
    private webSocketService: WebSocketService 
  ) {}

  ngOnInit(): void {
    const user = this.authService.getDecodedToken();
    if (user && user.tenantId) {
      this.tenantId = user.tenantId;
      this.loadDashboard();
      this.connectToWebSocket(); // 7. Connect when the component loads
    }
  }

  // 8. Add the connect method
  connectToWebSocket(): void {
    this.webSocketService.connect(this.tenantId);
    this.dashboardUpdateSubscription = this.webSocketService.dashboardUpdate$.subscribe(
      (updateMessage) => {
        console.log('Real-time update received from server:', updateMessage);
        this.toastr.info('The dashboard has been updated with new data.', 'Live Update');
        this.loadDashboard(); // This is the key: reload the data
      }
    );
  }

  // 9. Add the ngOnDestroy method for cleanup
  ngOnDestroy(): void {
    if (this.dashboardUpdateSubscription) {
      this.dashboardUpdateSubscription.unsubscribe();
    }
    this.webSocketService.disconnect();
  }

  loadDashboard(): void {
    this.isLoading = true;
    this.securityService.getTodaysDashboard(this.tenantId, this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        this.dashboardPage = data;
        this.isLoading = false;
      },
      error: () => {
        this.toastr.error('Failed to load dashboard data.');
        this.isLoading = false;
      },
    });
  }

  onPageChange(pageNumber: number): void {
    this.currentPage = pageNumber;
    this.loadDashboard();
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
    if (this.confirmationService.confirm('Check-in this visitor?')) {
      this.securityService.checkIn(this.tenantId, passId).subscribe({
        next: () => {
          this.toastr.success('Visitor checked in successfully.');
          this.loadDashboard();
          this.searchedPass = null;
          this.searchPassCode = '';
        },
        error: (err) => this.toastr.error(err.error.message || 'Failed to check-in visitor.'),
      });
    }
  }

  checkOut(passId: number): void {
    if (this.confirmationService.confirm('Check-out this visitor?')) {
      this.securityService.checkOut(this.tenantId, passId).subscribe({
        next: () => {
          this.toastr.info('Visitor checked out successfully.');
          this.loadDashboard();
        },
        error: (err) => this.toastr.error(err.error.message || 'Failed to check-out visitor.'),
      });
    }
  }
}