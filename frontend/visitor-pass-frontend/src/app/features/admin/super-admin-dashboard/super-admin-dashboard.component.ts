import { Component, OnInit, ViewChild, ElementRef, Inject } from '@angular/core';
import { TenantService } from '../../../core/services/tenant.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { SuperAdminDashboard } from '../../../core/models/tenant.model';
import { LoadingSpinnerComponent } from '../../../shared/loading-spinner/loading-spinner.component';
import { AuthService } from '../../../core/services/auth.service';
import { environment } from '../../../../environments/environment';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-super-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingSpinnerComponent],
  templateUrl: './super-admin-dashboard.component.html',
  styleUrls: ['./super-admin-dashboard.component.scss'] // Use .scss
})
export class SuperAdminDashboardComponent implements OnInit {
  // ViewChild allows us to get a reference to the modal's close button in the HTML
  @ViewChild('closeModalButton') closeModalButton!: ElementRef;

  dashboardData: SuperAdminDashboard | null = null;
  paginatedLocations: any = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 10, first: true, last: true };
  isLoading = true;
  isSubmitting = false;
  currentPage = 0;
  pageSize = 10;

  // This object will be bound to the form fields in the modal
  newTenantData: any = {};
  creatorName: string = '';

  // Expose Math to template
  Math = Math;

  // New admin creation after deletion
  showCreateAdminModal = false;
  deletedLocationInfo: any = null;
  newAdminData: any = {
    name: '',
    email: '',
    password: '',
    contact: ''
  };

  constructor(
    private tenantService: TenantService,
    @Inject(ToastrService) private toastr: ToastrService,
    private authService: AuthService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    console.log('=== SUPER ADMIN DASHBOARD INIT ===');

    // Check authentication first
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('No authentication token found');
      this.toastr.error('Authentication required. Please login again.');
      this.authService.logout();
      return;
    }

    const user = this.authService.getDecodedToken();
    console.log('Decoded user:', user);

    if (!user) {
      console.error('Invalid token');
      this.toastr.error('Invalid authentication token. Please login again.');
      this.authService.logout();
      return;
    }

    // Check if user has SUPER_ADMIN role (handle both single role and roles array)
    const userRoles = user.roles || [];
    const userRole = user.role || '';
    console.log('User roles array:', userRoles);
    console.log('User single role:', userRole);

    const isSuperAdmin = userRoles.includes('ROLE_SUPER_ADMIN') || userRole === 'ROLE_SUPER_ADMIN';
    console.log('Is Super Admin:', isSuperAdmin);

    if (!isSuperAdmin) {
      console.error('User does not have SUPER_ADMIN role');
      this.toastr.error('Access denied. Super Admin privileges required.');
      return;
    }

    // Check token expiration
    const currentTime = Date.now() / 1000;
    if (user.exp < currentTime) {
      console.error('Token has expired');
      this.toastr.error('Your session has expired. Please login again.');
      this.authService.logout();
      return;
    }

    console.log('Authentication checks passed');
    console.log('Token expires at:', new Date(user.exp * 1000));

    if (user && user.sub) {
        // Get the creator's name (email) for auditing purposes
        this.creatorName = user.sub;
    }

    this.loadDashboard();
    this.loadLocations();
  }

  loadDashboard(): void {
    this.isLoading = true;
    this.tenantService.getSuperAdminDashboard().subscribe({
      next: (data) => {
        this.dashboardData = data;
        this.isLoading = false;
      },
      error: () => {
        this.toastr.error('Failed to load dashboard analytics.');
        this.isLoading = false;
      }
    });
  }

  // VVV THIS IS THE MISSING METHOD VVV
  /**
   * Called when the "Create New Tenant" modal form is submitted.
   */
  onCreateTenant(): void {
    console.log('=== CREATING TENANT DEBUG ===');
    console.log('Token exists:', !!localStorage.getItem('token'));

    const token = localStorage.getItem('token');
    if (token) {
      try {
        const decoded = JSON.parse(atob(token.split('.')[1]));
        console.log('Decoded token:', decoded);
        console.log('User role:', decoded.role);
        console.log('Token expiry:', new Date(decoded.exp * 1000));
        console.log('Current time:', new Date());
        console.log('Token expired:', decoded.exp < Date.now() / 1000);
      } catch (e) {
        console.error('Error decoding token:', e);
      }
    }

    console.log('Tenant data to send:', this.newTenantData);

    this.isSubmitting = true;
    this.tenantService.createTenantAndAdmin(this.newTenantData).subscribe({
      next: () => {
        this.toastr.success(
          `🎉 Location "${this.newTenantData.tenantName}" created successfully!\n` +
          `📧 Comprehensive welcome email with login credentials sent to ${this.newTenantData.adminEmail}\n` +
          `👤 Admin account activated for ${this.newTenantData.adminName}`,
          '🏢 Location & Admin Created',
          {
            timeOut: 8000,
            enableHtml: true,
            closeButton: true
          }
        );
        this.loadDashboard(); // Refresh the dashboard data to show the new tenant
        this.closeModalButton.nativeElement.click(); // Programmatically close the modal
        this.newTenantData = {}; // Reset the form object for the next use
        this.isSubmitting = false;
      },
      error: (err) => {
        console.error('Tenant creation error:', err);

        // Safely extract error message
        let errorMessage = 'Failed to create tenant.';

        if (err.status === 401) {
          errorMessage = 'Authentication failed. Please login again.';
        } else if (err.status === 403) {
          errorMessage = 'Access denied. You may not have permission to create tenants.';
        } else if (err.error) {
          if (typeof err.error === 'string') {
            errorMessage = err.error;
          } else if (err.error.message) {
            errorMessage = err.error.message;
          } else if (err.error.error) {
            errorMessage = err.error.error;
          }
        } else if (err.message) {
          errorMessage = err.message;
        }

        this.toastr.error(errorMessage, 'Tenant Creation Failed', { timeOut: 5000 });
        this.isSubmitting = false;
      }
    });
  }

  loadLocations(): void {
    console.log('=== LOADING LOCATIONS ===');
    console.log('Current page:', this.currentPage);
    console.log('Page size:', this.pageSize);

    this.tenantService.getPaginatedLocations(this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        console.log('Locations loaded successfully:', data);
        this.paginatedLocations = data;
      },
      error: (error) => {
        console.error('=== LOAD LOCATIONS FAILED ===');
        console.error('Error status:', error.status);
        console.error('Error message:', error.message);
        console.error('Full error:', error);

        let errorMessage = 'Failed to load locations.';

        if (error.status === 401) {
          errorMessage = 'Authentication failed. Please login again.';
          this.authService.logout();
        } else if (error.status === 403) {
          errorMessage = 'Access denied. Super Admin privileges required.';
        } else if (error.status === 0) {
          errorMessage = 'Cannot connect to server. Please check if the backend is running.';
        }

        this.toastr.error(errorMessage);
      }
    });
  }

  loadPage(page: number): void {
    this.currentPage = page;
    this.loadLocations();
  }

  getPageNumbers(): number[] {
    const totalPages = this.paginatedLocations.totalPages;
    const currentPage = this.paginatedLocations.number;
    const pages: number[] = [];

    // Show max 5 page numbers
    const maxPages = 5;
    let startPage = Math.max(0, currentPage - Math.floor(maxPages / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxPages - 1);

    // Adjust start page if we're near the end
    if (endPage - startPage < maxPages - 1) {
      startPage = Math.max(0, endPage - maxPages + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    return pages;
  }

  deleteLocationAdmin(tenantId: number, locationName: string): void {
    console.log('=== DELETE LOCATION ADMIN ===');
    console.log('Tenant ID:', tenantId);
    console.log('Location Name:', locationName);
    console.log('Timestamp:', new Date().toISOString());

    // Step 1: Validate inputs
    if (!tenantId || tenantId <= 0) {
      this.toastr.error('Invalid tenant ID provided.');
      return;
    }

    if (!locationName || locationName.trim() === '') {
      this.toastr.error('Invalid location name provided.');
      return;
    }

    // Step 2: Check authentication
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

    // Step 3: Check if user has SUPER_ADMIN role (handle both single role and roles array)
    const userRoles = decodedToken.roles || [];
    const userRole = decodedToken.role || '';
    const isSuperAdmin = userRoles.includes('ROLE_SUPER_ADMIN') || userRole === 'ROLE_SUPER_ADMIN';

    if (!isSuperAdmin) {
      this.toastr.error('Access denied. Super Admin privileges required.');
      return;
    }

    // Step 4: Check token expiration
    const currentTime = Date.now() / 1000;
    if (decodedToken.exp < currentTime) {
      this.toastr.error('Your session has expired. Please login again.');
      this.authService.logout();
      return;
    }

    console.log('✅ Authentication check passed for SUPER_ADMIN');
    console.log('Token expires at:', new Date(decodedToken.exp * 1000));

    // Step 5: Confirm deletion with user
    const confirmMessage = `⚠️ DELETE CONFIRMATION ⚠️\n\nYou are about to delete the administrator for "${locationName}".\n\nThis action will:\n• Remove the admin user permanently\n• Require you to create a new admin\n• Cannot be undone\n\nAre you absolutely sure you want to proceed?`;

    if (confirm(confirmMessage)) {
      console.log('✅ User confirmed deletion, making API call...');

      // Step 6: Show loading state
      this.toastr.info(`Deleting administrator for "${locationName}"...`, 'Processing', { timeOut: 2000 });

      // Step 7: Make the API call
      this.tenantService.deleteLocationAdmin(tenantId).subscribe({
        next: (response: any) => {
          console.log('✅ DELETE API CALL SUCCESS:', response);

          // Success message
          this.toastr.success(`Administrator for "${locationName}" deleted successfully!`, 'Success');

          // Store location info for creating new admin
          this.deletedLocationInfo = {
            tenantId: tenantId,
            locationName: locationName
          };

          // Show modal to create new admin
          this.showCreateAdminModal = true;

          // Refresh data
          this.loadLocations();
          this.loadDashboard();
        },
        error: (err: any) => {
          console.error('❌ DELETE API CALL FAILED ===');
          console.error('Error status:', err.status);
          console.error('Error statusText:', err.statusText);
          console.error('Error message:', err.message);
          console.error('Error body:', err.error);
          console.error('Full error object:', err);

          // Handle different error types
          this.handleDeleteError(err, tenantId, locationName);
        }
      });
    } else {
      console.log('❌ User cancelled deletion');
      this.toastr.info('Deletion cancelled by user.');
    }
  }

  private handleDeleteError(err: any, tenantId: number, locationName: string): void {
    let errorMessage = 'Failed to delete location administrator.';
    let showWorkaround = false;

    switch (err.status) {
      case 400:
        errorMessage = `Invalid request: ${err.error?.error || err.error?.message || 'Bad request'}`;
        break;

      case 401:
        errorMessage = 'Authentication failed. Please login again.';
        this.authService.logout();
        return;

      case 403:
        errorMessage = 'Access denied. Super Admin privileges required.';
        break;

      case 404:
        errorMessage = `Location administrator not found for "${locationName}". It may have already been deleted.`;
        // Still show create admin option
        showWorkaround = true;
        break;

      case 409:
        errorMessage = `Cannot delete administrator due to existing dependencies. ${err.error?.details || 'Please remove related records first.'}`;
        break;

      case 500:
        errorMessage = `Server error occurred while deleting administrator. ${err.error?.error || err.error?.details || ''}`;
        showWorkaround = true;
        break;

      case 0:
        errorMessage = 'Cannot connect to server. Please check if the backend is running.';
        break;

      default:
        if (err.error?.error) {
          errorMessage = err.error.error;
        } else if (err.error?.message) {
          errorMessage = err.error.message;
        }
        showWorkaround = err.status >= 500;
        break;
    }

    // Show error message
    this.toastr.error(errorMessage, 'Delete Failed', { timeOut: 5000 });

    // Offer workaround for certain errors
    if (showWorkaround) {
      setTimeout(() => {
        const workaroundMessage = `The delete operation failed, but you can still create a new administrator for "${locationName}".\n\nWould you like to proceed with creating a new admin?\n\n(Note: You may need to manually verify the old admin was removed)`;

        if (confirm(workaroundMessage)) {
          this.proceedWithCreateAdmin(tenantId, locationName);
        }
      }, 2000);
    }
  }

  testAuthentication(): void {
    console.log('=== TESTING SUPER ADMIN AUTHENTICATION ===');

    const token = localStorage.getItem('token');
    console.log('Token exists:', !!token);

    if (!token) {
      this.toastr.error('No authentication token found.');
      return;
    }

    const decodedToken = this.authService.getDecodedToken();
    console.log('Decoded token:', decodedToken);

    if (!decodedToken) {
      this.toastr.error('Invalid token.');
      return;
    }

    const userRoles = decodedToken.roles || [];
    const userRole = decodedToken.role || '';
    console.log('User roles array:', userRoles);
    console.log('User single role:', userRole);

    const isSuperAdmin = userRoles.includes('ROLE_SUPER_ADMIN') || userRole === 'ROLE_SUPER_ADMIN';
    console.log('Is Super Admin:', isSuperAdmin);

    const currentTime = Date.now() / 1000;
    const isExpired = decodedToken.exp < currentTime;
    console.log('Token expired:', isExpired);
    console.log('Token expires at:', new Date(decodedToken.exp * 1000));

    if (isExpired) {
      this.toastr.error('Token has expired. Please login again.');
      this.authService.logout();
      return;
    }

    if (!isSuperAdmin) {
      this.toastr.error('Access denied. Super Admin role required.');
      return;
    }

    this.toastr.success('Authentication test passed! You have Super Admin access.');

    // Test the actual API call
    console.log('Testing actual locations API call...');
    this.tenantService.getPaginatedLocations(0, 1).subscribe({
      next: (response) => {
        console.log('Locations API test SUCCESS:', response);
        this.toastr.success('Locations API test passed!');
      },
      error: (error) => {
        console.error('Locations API test FAILED:', error);
        this.toastr.error(`Locations API test failed: ${error.status} - ${error.message}`);
      }
    });
  }

  createNewLocationAdmin(): void {
    console.log('Creating new location admin:', this.newAdminData);

    if (!this.deletedLocationInfo) {
      this.toastr.error('Location information missing.');
      return;
    }

    // Validate required fields
    if (!this.newAdminData.name || !this.newAdminData.email || !this.newAdminData.password) {
      this.toastr.error('Please fill in all required fields (Name, Email, Password).');
      return;
    }

    // Prepare admin data
    const adminData = {
      name: this.newAdminData.name.trim(),
      email: this.newAdminData.email.trim().toLowerCase(),
      password: this.newAdminData.password,
      role: 'ROLE_TENANT_ADMIN',
      contact: this.newAdminData.contact || '',
      department: '', // Empty for Super Admin created admins
      joiningDate: new Date().toISOString().split('T')[0], // Default to today
      address: '',
      gender: ''
    };

    console.log('Creating admin for tenant:', this.deletedLocationInfo.tenantId);
    console.log('Admin data:', adminData);

    // Use the test endpoint that works
    const createUrl = `${environment.apiUrl}/test/tenants/${this.deletedLocationInfo.tenantId}/users`;

    this.http.post(createUrl, adminData).subscribe({
      next: (response: any) => {
        console.log('New admin created successfully:', response);
        this.toastr.success(
          `👤 New administrator "${this.newAdminData.name}" created successfully!\n` +
          `🏢 Location: "${this.deletedLocationInfo.locationName}"\n` +
          `📧 Welcome email with credentials sent to ${this.newAdminData.email}`,
          '✅ Admin Account Created',
          {
            timeOut: 7000,
            enableHtml: true,
            closeButton: true
          }
        );

        // Close modal and reset
        this.closeCreateAdminModal();

        // Refresh data
        this.loadLocations();
        this.loadDashboard();
      },
      error: (error: any) => {
        console.error('Failed to create new admin:', error);

        let errorMessage = 'Failed to create new administrator.';

        if (error.status === 400) {
          // Handle validation errors including email uniqueness
          if (error.error && typeof error.error === 'string') {
            errorMessage = error.error;
          } else if (error.error?.message) {
            errorMessage = error.error.message;
          } else {
            errorMessage = 'Invalid input data. Please check all fields.';
          }
        } else if (error.status === 409) {
          errorMessage = 'Email address is already registered. Please use a different email.';
        } else if (error.error?.error) {
          errorMessage = error.error.error;
        } else if (error.message) {
          errorMessage = error.message;
        }

        this.toastr.error(errorMessage, 'Admin Creation Failed', { timeOut: 5000 });
      }
    });
  }

  closeCreateAdminModal(): void {
    this.showCreateAdminModal = false;
    this.deletedLocationInfo = null;
    this.newAdminData = {
      name: '',
      email: '',
      password: '',
      contact: ''
    };
  }

  skipCreateAdmin(): void {
    this.toastr.info(`Location "${this.deletedLocationInfo?.locationName}" now has no administrator.`);
    this.closeCreateAdminModal();
  }

  proceedWithCreateAdmin(tenantId: number, locationName: string): void {
    console.log('Proceeding with create admin workaround');

    // Store location info for creating new admin
    this.deletedLocationInfo = {
      tenantId: tenantId,
      locationName: locationName
    };

    // Show modal to create new admin
    this.showCreateAdminModal = true;

    this.toastr.info(`Creating new administrator for "${locationName}". Note: Please verify the old admin was actually deleted.`);
  }

  testBackendStatus(): void {
    console.log('Testing backend status...');

    // Test basic connectivity
    this.tenantService.getSuperAdminDashboard().subscribe({
      next: (response) => {
        console.log('Backend connectivity test SUCCESS:', response);
        this.toastr.success('Backend is running and accessible!');
      },
      error: (error) => {
        console.error('Backend connectivity test FAILED:', error);
        if (error.status === 0) {
          this.toastr.error('Backend is not running or not accessible!');
        } else {
          this.toastr.error(`Backend error: ${error.status} - ${error.message}`);
        }
      }
    });
  }
}
