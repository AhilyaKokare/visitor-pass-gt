import { Component, OnInit, ViewChild, ElementRef, NgZone } from '@angular/core';
import { CommonModule, TitleCasePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { User } from '../../../core/models/user.model';
import { LoadingSpinnerComponent } from '../../../shared/loading-spinner/loading-spinner.component';
import { ConfirmationService } from '../../../core/services/confirmation.service';
import { Page, PaginationComponent } from '../../../shared/pagination/pagination.component';
import { ValidationService } from '../../../core/services/validation.service';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, RouterModule, TitleCasePipe, LoadingSpinnerComponent, FormsModule, PaginationComponent],
  templateUrl: './user-list.component.html',
})
export class UserListComponent implements OnInit {

  @ViewChild('closeModalButton') closeModalButton!: ElementRef;

  // THIS IS THE SINGLE SOURCE OF TRUTH FOR THE VIEW
  userPageDetails: Page<User> | null = null;

  tenantId!: number;
  isLoading = true;
  isSubmitting = false;
  currentPage = 0;
  pageSize = 10;

  newUser: any = {
    role: 'ROLE_EMPLOYEE',
    joiningDate: new Date().toISOString().split('T')[0] // Default to today
  };
  maxDate: string = new Date().toISOString().split('T')[0];

  validationErrors: { [key: string]: string } = {};

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private toastr: ToastrService,
    private confirmationService: ConfirmationService,
    private zone: NgZone,
    private validationService: ValidationService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getDecodedToken();
    if (user && user.tenantId) {
      this.tenantId = user.tenantId;
      this.loadUsers();
    } else {
      this.toastr.error('Authentication error. Please login again.');
      this.authService.logout();
    }
  }

  loadUsers(): void {
    this.isLoading = true;
    this.userService.getUsers(this.tenantId, this.currentPage, this.pageSize).subscribe({
      next: data => {
        this.userPageDetails = data;
        this.isLoading = false;
      },
      error: () => {
        this.toastr.error('Failed to load users.');
        this.isLoading = false;
      }
    });
  }

  onPageChange(pageNumber: number): void {
    this.currentPage = pageNumber;
    this.loadUsers();
  }

  onCreateUser(): void {
    if (!this.validateForm()) {
      const firstError = Object.values(this.validationErrors)[0];
      this.toastr.error(firstError || 'Please fix the validation errors before submitting.');
      return;
    }

    this.isSubmitting = true;
    this.userService.createUser(this.tenantId, this.newUser).subscribe({
      next: () => {
        this.toastr.success('User created successfully!');
        this.currentPage = 0; // Go back to the first page to see the new user
        this.loadUsers();
        this.closeModalButton.nativeElement.click();
        this.newUser = { // Reset form
          role: 'ROLE_EMPLOYEE',
          joiningDate: new Date().toISOString().split('T')[0]
        };
        this.isSubmitting = false;
      },
      error: (err) => {
        const errorMessage = err.error?.message || err.error || 'Failed to create user. The email may already be in use.';
        this.toastr.error(errorMessage, 'Creation Failed');
        this.isSubmitting = false;
      }
    });
  }

  toggleUserStatus(user: User): void {
    const action = user.isActive ? 'Deactivate' : 'Activate';
    if (this.confirmationService.confirm(`Are you sure you want to ${action} user "${user.name}"?`)) {
      this.userService.updateUserStatus(this.tenantId, user.id, !user.isActive).subscribe({
        next: (updatedUser) => {
          this.toastr.success(`User has been ${action.toLowerCase()}d.`);
          // For immediate UI update without a full reload
          this.zone.run(() => {
            if (this.userPageDetails && this.userPageDetails.content) {
              const index = this.userPageDetails.content.findIndex(u => u.id === updatedUser.id);
              if (index !== -1) {
                this.userPageDetails.content[index] = updatedUser;
              }
            }
          });
        },
        error: (err) => {
          this.toastr.error(err.error?.message || 'Failed to update user status.');
        }
      });
    }
  }

  validateForm(): boolean {
    this.validationErrors = {};
    // A temporary boolean that we'll set to false if any validation fails
    let isFormValid = true;

    const nameValidation = this.validationService.validateName(this.newUser.name);
    if (!nameValidation.isValid) {
      this.validationErrors['name'] = nameValidation.errorMessage || '';
      isFormValid = false;
    }

    const emailValidation = this.validationService.validateEmail(this.newUser.email);
    if (!emailValidation.isValid) {
      this.validationErrors['email'] = emailValidation.errorMessage || '';
      isFormValid = false;
    }

    const passwordValidation = this.validationService.validatePassword(this.newUser.password);
    if (!passwordValidation.isValid) {
      this.validationErrors['password'] = passwordValidation.errorMessage || '';
      isFormValid = false;
    }

    const mobileValidation = this.validationService.validateMobile(this.newUser.contact, false);
    if (!mobileValidation.isValid) {
      this.validationErrors['contact'] = mobileValidation.errorMessage || '';
      isFormValid = false;
    }

    // **** THIS IS THE FIX: Remove the duplicate "return" ****
    return isFormValid;
  }

  onFieldChange(fieldName: string): void {
    if (this.validationErrors[fieldName]) {
      delete this.validationErrors[fieldName];
    }
    // Re-validation logic can be added here if desired for real-time feedback
  }

  hasValidationError(fieldName: string): boolean {
    return !!this.validationErrors[fieldName];
  }

  getValidationError(fieldName: string): string {
    return this.validationErrors[fieldName] || '';
  }
}
