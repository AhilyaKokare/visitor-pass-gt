import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss']
})
export class ForgotPasswordComponent {
  forgotPasswordForm: FormGroup;
  isSubmitting = false;
  isEmailSent = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.forgotPasswordForm.valid) {
      this.isSubmitting = true;
      
      this.authService.forgotPassword(this.forgotPasswordForm.value).subscribe({
        next: (response) => {
          this.isEmailSent = true;
          this.isSubmitting = false;
          this.toastr.success('Password reset email sent successfully!');
        },
        error: (error) => {
          this.isSubmitting = false;
          console.error('Forgot password error:', error);
          let errorMessage = 'An error occurred. Please try again.';

          if (error.error) {
            errorMessage = typeof error.error === 'string' ? error.error : error.error.message || errorMessage;
          } else if (error.message) {
            errorMessage = error.message;
          }

          this.toastr.error(errorMessage);
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  private markFormGroupTouched(): void {
    Object.keys(this.forgotPasswordForm.controls).forEach(key => {
      const control = this.forgotPasswordForm.get(key);
      control?.markAsTouched();
    });
  }

  goBackToLogin(): void {
    this.router.navigate(['/login']);
  }
}
