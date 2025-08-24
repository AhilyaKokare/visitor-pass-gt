import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ToastrService } from 'ngx-toastr';
import { PassService } from '../../../core/services/pass.service';
import { AuthService } from '../../../core/services/auth.service';
import { VisitorPass } from '../../../core/models/pass.model';
import { LoadingSpinnerComponent } from '../../../shared/loading-spinner/loading-spinner.component';
import { ConfirmationService } from '../../../core/services/confirmation.service';
import { Page, PaginationComponent } from '../../../shared/pagination/pagination.component';
import { StateService } from '../../../core/services/state.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-approval-queue',
  standalone: true,
  imports: [CommonModule, LoadingSpinnerComponent, PaginationComponent, DatePipe],
  templateUrl: './approval-queue.component.html',
})
export class ApprovalQueueComponent implements OnInit, OnDestroy {
  
  passPageDetails: Page<VisitorPass> | null = null;
  tenantId!: number;
  isLoading = true;
  currentPage = 0;
  pageSize = 10;
  private passChangeSubscription?: Subscription;

  constructor(
    private passService: PassService,
    private authService: AuthService,
    private toastr: ToastrService,
    private confirmationService: ConfirmationService,
    private stateService: StateService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getDecodedToken();
    if (user && user.tenantId) {
      this.tenantId = user.tenantId;
      this.loadPasses();

      this.passChangeSubscription = this.stateService.passChange$.subscribe(() => {
        console.log('ApprovalQueue received notification, reloading.');
        this.loadPasses();
      });

    } else {
      this.toastr.error('Could not identify your location. Please log in again.');
      this.isLoading = false;
    }
  }

  ngOnDestroy(): void {
    this.passChangeSubscription?.unsubscribe();
  }

  loadPasses(): void {
    this.isLoading = true;
    this.passService.getPassesForTenant(this.tenantId, this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        this.passPageDetails = data;
        this.isLoading = false;
      },
      error: () => {
        this.toastr.error('Failed to load pass data for approval.');
        this.isLoading = false;
      }
    });
  }

  onPageChange(pageNumber: number): void {
    this.currentPage = pageNumber;
    this.loadPasses();
  }

  approve(passId: number): void {
    if (this.confirmationService.confirm('Are you sure you want to approve this pass?')) {
      this.passService.approvePass(this.tenantId, passId).subscribe({
        next: () => {
          this.toastr.success('Pass approved successfully!');
          
          // --- THIS IS THE CRITICAL FIX ---
          // 1. Reset pagination to the first page.
          this.currentPage = 0;
          // 2. Reload data for this component. This will now correctly show the updated queue.
          this.loadPasses();
          // 3. Notify all other components of the change.
          this.stateService.notifyPassChange();
        },
        error: (err) => this.toastr.error(err.error?.message || 'Failed to approve pass.'),
      });
    }
  }

  reject(passId: number): void {
    const reason = prompt('Please provide a reason for rejection (required):');
    if (reason && reason.trim()) {
      this.passService.rejectPass(this.tenantId, passId, reason).subscribe({
        next: () => {
          this.toastr.info('Pass has been rejected.');

          // --- THIS IS THE CRITICAL FIX ---
          // 1. Reset pagination to the first page.
          this.currentPage = 0;
          // 2. Reload data for this component.
          this.loadPasses();
          // 3. Notify all other components.
          this.stateService.notifyPassChange();
        },
        error: (err) => this.toastr.error(err.error?.message || 'Failed to reject pass.'),
      });
    } else if (reason !== null) {
        this.toastr.warning('A reason is required to reject a pass.');
    }
  }
}