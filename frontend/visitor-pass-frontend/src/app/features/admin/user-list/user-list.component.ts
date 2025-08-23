// 1. IMPORT NgZone and other necessary modules
import { Component, OnInit, NgZone, ViewChild, ElementRef } from '@angular/core';
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
import { environment } from '../../../../environments/environment';
import { ValidationService, ValidationResult } from '../../../core/services/validation.service';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, RouterModule, TitleCasePipe, LoadingSpinnerComponent, FormsModule, PaginationComponent],
  templateUrl: './user-list.component.html',
})
export class UserListComponent implements OnInit {

  // --- RESTORED @ViewChild ---
  @ViewChild('closeModalButton') closeModalButton!: ElementRef;

  users: User[] = [];
  userPageDetails: Page<any> | null = null;
  tenantId!: number;
  isLoading = true;
  isSubmitting = false;
  currentPage = 0;
  pageSize = 10;
  newUser: any = {
    role: 'ROLE_EMPLOYEE',
    joiningDate: new Date().toISOString().split('T')[0] // Today's date in YYYY-MM-DD format
  };
  maxDate: string = new Date().toISOString().split('T')[0]; // Today's date as max date

  // Validation properties
  validationErrors: { [key: string]: string } = {};
  isFormValid = true;

  // 2. INJECT NgZone
  constructor(
    private userService: UserService,
    private authService: AuthService,
    private toastr: ToastrService,
    private confirmationService: ConfirmationService,
    private zone: NgZone,
    private validationService: ValidationService
  ) {}

  ngOnInit(): void {
    console.log('UserListComponent ngOnInit called');

    // Check if user is logged in
    if (!this.authService.isLoggedIn()) {
      console.error('User not logged in');
      this.toastr.error('Please login to access this page.');
      this.authService.logout();
      return;
    }

    const user = this.authService.getDecodedToken();
    console.log('Decoded user:', user);

    if (user && user.tenantId) {
      this.tenantId = user.tenantId;
      console.log('Loading users for tenant:', this.tenantId);
      this.loadUsers();
    } else {
      console.error('No user or tenantId found in token');
      this.toastr.error('Authentication error. Please login again.');
      this.authService.logout();
    }
  }

  loadUsers(): void {
    this.isLoading = true;
    console.log('=== LOADING USERS ===');
    console.log('Tenant ID:', this.tenantId);
    console.log('Current page:', this.currentPage);
    console.log('Page size:', this.pageSize);

    this.userService.getUsers(this.tenantId, this.currentPage, this.pageSize).subscribe({
      next: data => {
        console.log('=== USERS LOADED SUCCESSFULLY ===');
        console.log('Total users:', data.totalElements);
        console.log('Users on this page:', data.content.length);
        console.log('User list:', data.content);
        console.log('User emails:', data.content.map(u => u.email));

        this.users = data.content;
        this.userPageDetails = data;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('=== ERROR LOADING USERS ===');
        console.error('Error:', error);
        this.toastr.error('Failed to load users.');
        this.isLoading = false;
      }
    });
  }

  onPageChange(pageNumber: number): void {
    this.currentPage = pageNumber;
    this.loadUsers();
  }

  // --- RESTORED onCreateUser METHOD ---
  onCreateUser(): void {
    console.log('=== MAIN FORM SUBMISSION ===');

    // Check authentication first
    const token = localStorage.getItem('token');
    if (!token) {
      this.toastr.error('Authentication token missing. Please login again.');
      this.authService.logout();
      return;
    }

    const decodedToken = this.authService.getDecodedToken();
    if (!decodedToken) {
      this.toastr.error('Invalid authentication token. Please login again.');
      this.authService.logout();
      return;
    }

    // Check token expiration
    const currentTime = Date.now() / 1000;
    if (decodedToken.exp < currentTime) {
      this.toastr.error('Your session has expired. Please login again.');
      this.authService.logout();
      return;
    }

    console.log('Authentication check passed');
    console.log('Raw newUser object:', this.newUser);
    console.log('Tenant ID:', this.tenantId);
    console.log('Token expires at:', new Date(decodedToken.exp * 1000));

    // Validate form using validation service
    if (!this.validateForm()) {
      console.error('Form validation failed');
      const firstError = Object.values(this.validationErrors)[0];
      this.toastr.error(firstError || 'Please fix the validation errors.');
      return;
    }

    // Additional required field check
    if (!this.newUser.joiningDate) {
      console.error('Joining date is required');
      this.toastr.error('Joining date is required.');
      return;
    }

    // Validate email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.newUser.email)) {
      this.toastr.error('Please enter a valid email address.');
      return;
    }

    // Validate password length
    if (this.newUser.password.length < 6) {
      this.toastr.error('Password must be at least 6 characters long.');
      return;
    }

    // Prepare the data for backend - ensure all required fields are present
    const userData = {
      name: this.newUser.name.trim(),
      email: this.newUser.email.trim().toLowerCase(),
      password: this.newUser.password,
      role: this.newUser.role || 'ROLE_EMPLOYEE',
      department: '', // Empty for Tenant Admin created users
      joiningDate: this.newUser.joiningDate, // Keep as string, backend will parse
      contact: this.newUser.contact || '',
      gender: this.newUser.gender || '',
      address: this.newUser.address || ''
    };

    console.log('Prepared user data for API:', userData);

    this.isSubmitting = true;

    // Temporarily use test endpoint since main endpoint has auth issues
    console.log('Using test endpoint due to auth issues with main endpoint');
    const testUrl = `${environment.apiUrl}/test/tenants/${this.tenantId}/users`;

    this.userService.http.post(testUrl, userData).subscribe({
      next: (response) => {
        console.log('User created successfully:', response);
        this.toastr.success('User created successfully!');
        this.loadUsers(); // Reload the list to show the new user
        this.closeModalButton.nativeElement.click(); // Close the modal
        this.newUser = {
          role: 'ROLE_EMPLOYEE',
          joiningDate: new Date().toISOString().split('T')[0]
        }; // Reset the form
        this.isSubmitting = false;
      },
      error: (err) => {
        console.error('=== MAIN FORM ERROR ===');
        console.error('Error status:', err.status);
        console.error('Error message:', err.message);
        console.error('Full error object:', err);

        let errorMessage = 'Failed to create user.';

        if (err.status === 400) {
          // Handle validation errors including email uniqueness
          if (err.error && typeof err.error === 'string') {
            errorMessage = err.error;
          } else if (err.error?.message) {
            errorMessage = err.error.message;
          } else {
            errorMessage = 'Invalid input data. Please check all fields.';
          }
        } else if (err.status === 401) {
          errorMessage = 'Authentication failed. Your session may have expired. Please login again.';
          this.authService.logout();
        } else if (err.status === 403) {
          errorMessage = 'Access denied. You may not have permission to create users.';
        } else if (err.status === 409) {
          errorMessage = 'Email address is already registered. Please use a different email.';
        } else if (err.status === 0) {
          errorMessage = 'Cannot connect to server. Please check if the backend is running.';
        } else if (err.error) {
          if (typeof err.error === 'string') {
            errorMessage = err.error;
          } else if (err.error.message) {
            errorMessage = err.error.message;
          } else if (err.error.error) {
            errorMessage = err.error.error;
          }
        }

        this.toastr.error(errorMessage, 'User Creation Failed', { timeOut: 5000 });
        this.isSubmitting = false;
      }
    });
  }

  debugForm(): void {
    console.log('=== FORM DEBUG INFO ===');
    console.log('newUser object:', this.newUser);
    console.log('tenantId:', this.tenantId);
    console.log('isSubmitting:', this.isSubmitting);
    console.log('Current token:', localStorage.getItem('token'));
    console.log('Decoded token:', this.authService.getDecodedToken());
    console.log('API URL would be:', `${environment.apiUrl}/tenants/${this.tenantId}/admin/users`);
    console.log('========================');

    // Test API connectivity
    this.testApiConnectivity();

    // Show in toast as well
    this.toastr.info(`Debug: TenantId=${this.tenantId}, User=${JSON.stringify(this.newUser)}`, 'Form Debug', { timeOut: 10000 });
  }

  showFormData(): void {
    console.log('=== CURRENT FORM DATA ===');
    console.log('newUser object:', this.newUser);
    console.log('JSON:', JSON.stringify(this.newUser, null, 2));

    const formData = `
Name: ${this.newUser.name || 'EMPTY'}
Email: ${this.newUser.email || 'EMPTY'}
Password: ${this.newUser.password || 'EMPTY'}
Role: ${this.newUser.role || 'EMPTY'}
Joining Date: ${this.newUser.joiningDate || 'EMPTY'}
Contact: ${this.newUser.contact || 'EMPTY'}
Gender: ${this.newUser.gender || 'EMPTY'}
Address: ${this.newUser.address || 'EMPTY'}
    `;

    alert('Current Form Data:\n' + formData);
  }

  testBackend(): void {
    console.log('Testing backend connectivity and authentication...');

    // Check token first
    const token = localStorage.getItem('token');
    console.log('Current token:', token ? 'EXISTS' : 'MISSING');

    if (!token) {
      this.toastr.error('No authentication token found. Please login again.');
      this.authService.logout();
      return;
    }

    // Check if token is expired
    const decodedToken = this.authService.getDecodedToken();
    console.log('Decoded token:', decodedToken);

    if (!decodedToken) {
      this.toastr.error('Invalid token. Please login again.');
      this.authService.logout();
      return;
    }

    // Check token expiration
    const currentTime = Date.now() / 1000;
    if (decodedToken.exp < currentTime) {
      this.toastr.error('Token has expired. Please login again.');
      this.authService.logout();
      return;
    }

    console.log('Token is valid. Testing API calls...');

    // Test 1: Simple GET request to check if backend is alive
    this.userService.http.get(`${environment.apiUrl}/test/health`).subscribe({
      next: (response) => {
        console.log('Backend health check SUCCESS:', response);
        this.toastr.success('Backend is running and accessible!');

        // Test 2: Check if we can get users with authentication
        this.testApiConnectivity();
      },
      error: (error) => {
        console.error('Backend health check FAILED:', error);
        if (error.status === 0) {
          this.toastr.error('Backend is not running or not accessible!');
        } else if (error.status === 401) {
          this.toastr.error('Authentication failed. Please login again.');
          this.authService.logout();
        } else {
          this.toastr.error(`Backend error: ${error.status} - ${error.message}`);
        }
      }
    });
  }

  testApiConnectivity(): void {
    console.log('Testing API connectivity...');
    this.userService.getUsers(this.tenantId, 0, 1).subscribe({
      next: (response) => {
        console.log('API connectivity test SUCCESS:', response);
        this.toastr.success('API connectivity test passed!');
      },
      error: (error) => {
        console.error('API connectivity test FAILED:', error);
        this.toastr.error(`API test failed: ${error.status} - ${error.message}`);
      }
    });
  }

  testCreateUser(): void {
    console.log('Testing user creation with test endpoint using FORM DATA...');

    // Use actual form data instead of hardcoded test data
    const testData = {
      name: this.newUser.name || 'Test User',
      email: this.newUser.email || 'test@example.com',
      password: this.newUser.password || 'password123',
      role: this.newUser.role || 'ROLE_EMPLOYEE',
      department: '', // Empty for Tenant Admin created users
      joiningDate: this.newUser.joiningDate || '2024-01-01',
      contact: this.newUser.contact || '+1234567890',
      gender: this.newUser.gender || 'Male',
      address: this.newUser.address || '123 Test Street'
    };

    const url = `${environment.apiUrl}/test/tenants/${this.tenantId}/users`;
    console.log('Test URL:', url);
    console.log('Form Data from newUser:', this.newUser);
    console.log('Final Test Data being sent:', testData);

    this.userService.http.post(url, testData).subscribe({
      next: (response) => {
        console.log('=== API CALL SUCCESS ===');
        console.log('Response:', response);
        console.log('Response type:', typeof response);
        console.log('Response keys:', Object.keys(response || {}));

        this.toastr.success('User created successfully! Check the user list.');

        // Wait a moment then refresh the user list
        setTimeout(() => {
          console.log('Refreshing user list...');
          this.loadUsers();
        }, 1000);

        this.closeModalButton.nativeElement.click(); // Close the modal
        this.newUser = {
          role: 'ROLE_EMPLOYEE',
          joiningDate: new Date().toISOString().split('T')[0]
        }; // Reset the form
      },
      error: (error) => {
        console.error('=== API CALL FAILED ===');
        console.error('Error status:', error.status);
        console.error('Error message:', error.message);
        console.error('Error body:', error.error);
        console.error('Full error:', error);

        let errorMessage = 'Failed to create user.';
        if (error.status === 0) {
          errorMessage = 'Cannot connect to server. Is the backend running?';
        } else if (error.status === 401) {
          errorMessage = 'Authentication failed. Please login again.';
        } else if (error.status === 403) {
          errorMessage = 'Access denied. Check your permissions.';
        } else if (error.error?.error) {
          errorMessage = error.error.error;
        } else if (error.error?.message) {
          errorMessage = error.error.message;
        }

        this.toastr.error(errorMessage);
      }
    });
  }

  toggleUserStatus(userToToggle: User): void {
    const action = userToToggle.isActive ? 'Deactivate' : 'Activate';
    const newStatus = !userToToggle.isActive;

    if (this.confirmationService.confirm(`Are you sure you want to ${action} user "${userToToggle.name}"?`)) {
      this.userService.updateUserStatus(this.tenantId, userToToggle.id, newStatus).subscribe({
        next: (updatedUserFromServer) => {
          
          // 3. WRAP the UI update logic inside zone.run()
          this.zone.run(() => {
            this.toastr.success(`User has been ${action.toLowerCase()}d.`);
            
            const index = this.users.findIndex(u => u.id === updatedUserFromServer.id);
            if (index !== -1) {
              const newUsers = [...this.users];
              newUsers[index] = updatedUserFromServer;
              this.users = newUsers; 
            } else {
              this.loadUsers(); // Fallback
            }
          });
        },
        error: (err) => {
          this.toastr.error(err?.error?.message || 'Failed to update user status.');
        }
      });
    }
  }

  // Validation methods
  validateForm(): boolean {
    this.validationErrors = {};
    this.isFormValid = true;

    // Validate name
    const nameValidation = this.validationService.validateName(this.newUser.name);
    if (!nameValidation.isValid) {
      this.validationErrors['name'] = nameValidation.errorMessage || '';
      this.isFormValid = false;
    }

    // Validate email
    const emailValidation = this.validationService.validateEmail(this.newUser.email);
    if (!emailValidation.isValid) {
      this.validationErrors['email'] = emailValidation.errorMessage || '';
      this.isFormValid = false;
    }

    // Validate password
    const passwordValidation = this.validationService.validatePassword(this.newUser.password);
    if (!passwordValidation.isValid) {
      this.validationErrors['password'] = passwordValidation.errorMessage || '';
      this.isFormValid = false;
    }

    // Validate mobile (optional)
    const mobileValidation = this.validationService.validateMobile(this.newUser.contact, false);
    if (!mobileValidation.isValid) {
      this.validationErrors['contact'] = mobileValidation.errorMessage || '';
      this.isFormValid = false;
    }

    return this.isFormValid;
  }

  onFieldChange(fieldName: string): void {
    // Clear validation error for this field when user starts typing
    if (this.validationErrors[fieldName]) {
      delete this.validationErrors[fieldName];
    }

    // Re-validate the specific field
    switch (fieldName) {
      case 'name':
        const nameValidation = this.validationService.validateName(this.newUser.name);
        if (!nameValidation.isValid) {
          this.validationErrors['name'] = nameValidation.errorMessage || '';
        }
        break;
      case 'email':
        const emailValidation = this.validationService.validateEmail(this.newUser.email);
        if (!emailValidation.isValid) {
          this.validationErrors['email'] = emailValidation.errorMessage || '';
        }
        break;
      case 'password':
        const passwordValidation = this.validationService.validatePassword(this.newUser.password);
        if (!passwordValidation.isValid) {
          this.validationErrors['password'] = passwordValidation.errorMessage || '';
        }
        break;
      case 'contact':
        const mobileValidation = this.validationService.validateMobile(this.newUser.contact, false);
        if (!mobileValidation.isValid) {
          this.validationErrors['contact'] = mobileValidation.errorMessage || '';
        }
        break;
    }
  }

  hasValidationError(fieldName: string): boolean {
    return !!this.validationErrors[fieldName];
  }

  getValidationError(fieldName: string): string {
    return this.validationErrors[fieldName] || '';
  }
}