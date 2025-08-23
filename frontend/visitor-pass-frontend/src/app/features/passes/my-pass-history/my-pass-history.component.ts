// FILE: frontend/visitor-pass-frontend/src/app/features/passes/my-pass-history/my-pass-history.component.ts

import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../../core/services/auth.service';
import { PassService } from '../../../core/services/pass.service';
import { VisitorPass } from '../../../core/models/pass.model';
import { LoadingSpinnerComponent } from '../../../shared/loading-spinner/loading-spinner.component';
import { Page } from '../../../core/models/page.model';
import { PaginationComponent } from '../../../shared/pagination/pagination.component'; // <-- DOUBLE CHECK THIS PATH

@Component({
  selector: 'app-my-pass-history',
  standalone: true,
  imports: [
    CommonModule, 
    DatePipe, 
    LoadingSpinnerComponent, 
    PaginationComponent // <-- ENSURE THIS COMPONENT IS CORRECTLY IMPORTED
  ],
  templateUrl: './my-pass-history.component.html',
})
export class MyPassHistoryComponent implements OnInit {
  
  passHistoryPage: Page<VisitorPass> | null = null;
  currentPage = 0;
  pageSize = 10;
  
  isLoading = true;
  tenantId!: number;
  selectedPass: VisitorPass | null = null;

  constructor(
    private passService: PassService,
    private authService: AuthService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getDecodedToken();
    if (user && user.tenantId) {
      this.tenantId = user.tenantId;
      this.loadHistory();
    }
  }

  loadHistory(): void {
    this.isLoading = true;
    this.passService.getMyPassHistory(this.tenantId, this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        this.passHistoryPage = data;
        this.isLoading = false;
      },
      error: () => {
        this.toastr.error('Failed to load pass history.');
        this.isLoading = false;
      }
    });
  }
  
  onPageChange(pageNumber: number): void {
    this.currentPage = pageNumber;
    this.loadHistory();
  }

  viewDetails(pass: VisitorPass): void {
    this.selectedPass = pass;
  }
}