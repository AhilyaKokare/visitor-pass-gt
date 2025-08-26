// FILE: frontend/visitor-pass-frontend/src/app/features/security/security-dashboard/security-dashboard.component.ts

import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { SecurityService } from '../../../core/services/security.service';
import { AuthService } from '../../../core/services/auth.service';
import { VisitorPass } from '../../../core/models/pass.model';
import { LoadingSpinnerComponent } from '../../../shared/loading-spinner/loading-spinner.component';
import { Page } from '../../../core/models/page.model';
import { PaginationComponent } from '../../../shared/pagination/pagination.component';
import { WebSocketService } from '../../../core/services/websocket.service';
import { Subscription } from 'rxjs';
import { finalize } from 'rxjs/operators'; // VVV --- IMPORT finalize --- VVV
import { Modal } from 'bootstrap';

@Component({
  selector: 'app-security-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingSpinnerComponent, PaginationComponent, DatePipe],
  templateUrl: './security-dashboard.component.html',
})
export class SecurityDashboardComponent implements OnInit, OnDestroy, AfterViewInit {
  
  @ViewChild('searchModal') searchModalElement!: ElementRef;
  private searchResultModal!: Modal;

  // VVV --- ENSURE THIS PROPERTY IS DECLARED --- VVV
  dashboardPage: Page<VisitorPass> | null = null;
  
  currentPage = 0;
  pageSize = 10;
  searchPassCode: string = '';
  searchedPass: VisitorPass | null = null;
  isLoading = true;
  isSearching = false;
  tenantId!: number;
  private dashboardUpdateSubscription!: Subscription;

  constructor(
    private securityService: SecurityService,
    private authService: AuthService,
    private toastr: ToastrService,
    private webSocketService: WebSocketService 
  ) {}

  ngOnInit(): void {
    const user = this.authService.getDecodedToken();
    if (user && user.tenantId) {
      this.tenantId = user.tenantId;
      this.loadDashboard();
      this.connectToWebSocket();
    }
  }

  ngAfterViewInit(): void {
    if (this.searchModalElement) {
      this.searchResultModal = new Modal(this.searchModalElement.nativeElement);
    }
  }

  connectToWebSocket(): void {
    this.webSocketService.connect(this.tenantId);
    this.dashboardUpdateSubscription = this.webSocketService.dashboardUpdate$.subscribe(
      () => {
        this.toastr.info('The dashboard has been updated with new data.', 'Live Update');
        this.loadDashboard();
      }
    );
  }

  ngOnDestroy(): void {
    if (this.dashboardUpdateSubscription) {
      this.dashboardUpdateSubscription.unsubscribe();
    }
    this.webSocketService.disconnect();
  }

  // VVV --- THIS IS THE CORRECTED, ROBUST METHOD --- VVV
  loadDashboard(): void {
    this.isLoading = true;
    this.securityService.getTodaysDashboard(this.tenantId, this.currentPage, this.pageSize)
      .pipe(
        finalize(() => {
          this.isLoading = false; // This will now ALWAYS run
        })
      )
      .subscribe({
        next: (data) => {
          this.dashboardPage = data;
        },
        error: () => {
          this.toastr.error('Failed to load dashboard data.');
        }
      });
  }

  onPageChange(pageNumber: number): void {
    this.currentPage = pageNumber;
    this.loadDashboard();
  }
  
  onSearch(): void {
    if (!this.searchPassCode.trim()) { return; }
    this.isSearching = true;
    this.securityService.searchPassByCode(this.tenantId, this.searchPassCode).subscribe({
      next: (data) => {
        this.searchedPass = data;
        this.isSearching = false;
        this.searchResultModal?.show();
      },
      error: () => {
        this.toastr.error(`No valid pass found with code: ${this.searchPassCode}`);
        this.searchedPass = null;
        this.isSearching = false;
      },
    });
  }

  checkIn(passId: number): void {
    this.securityService.checkIn(this.tenantId, passId).subscribe({
      next: () => {
        this.toastr.success('Visitor checked in successfully.');
        this.loadDashboard();
        this.searchResultModal?.hide();
      },
      error: (err) => this.toastr.error(err.error.message || 'Failed to check-in visitor.'),
    });
  }

  checkOut(passId: number): void {
    this.securityService.checkOut(this.tenantId, passId).subscribe({
      next: () => {
        this.toastr.info('Visitor checked out successfully.');
        this.loadDashboard();
        this.searchResultModal?.hide();
      },
      error: (err) => this.toastr.error(err.error.message || 'Failed to check-out visitor.'),
    });
  }
}