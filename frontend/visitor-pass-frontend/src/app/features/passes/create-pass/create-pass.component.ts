import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

import { PassService } from '../../../core/services/pass.service';
import { AuthService } from '../../../core/services/auth.service';
import { CreatePassPayload } from '../../../core/models/pass.model';

@Component({
  selector: 'app-create-pass',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-pass.component.html',
})
export class CreatePassComponent {
  // Initialize with the CreatePassPayload interface for type safety
  passData: CreatePassPayload = {
    visitorName: '',
    visitorEmail: '',
    visitorPhone: '',
    purpose: '',
    visitDateTime: ''
  };

  tenantId!: number;
  isSubmitting = false;

  constructor(
    private passService: PassService,
    private authService: AuthService,
    private toastr: ToastrService,
    private router: Router
  ) {
    const user = this.authService.getDecodedToken();
    if (user && user.tenantId) {
      this.tenantId = user.tenantId;
    } else {
      this.toastr.error('Could not identify your location. Please log in again.');
    }
  }

  onCreatePass(): void {
    if (!this.tenantId) {
      this.toastr.error('User tenant not found. Cannot create pass.');
      return;
    }
    this.isSubmitting = true;

    // The passData object now correctly matches the required API payload
    this.passService.createPass(this.tenantId, this.passData).subscribe({
      next: () => {
        this.toastr.success('Visitor pass request submitted successfully!');
        this.router.navigate(['/passes/my-pass-history']); // Navigate to a more relevant page
        this.isSubmitting = false;
      },
      error: (err) => {
        // More specific error handling
        const errorMessage = err?.error?.message || 'Failed to submit pass request. Please check the details and try again.';
        this.toastr.error(errorMessage);
        this.isSubmitting = false;
      }
    });
  }
}