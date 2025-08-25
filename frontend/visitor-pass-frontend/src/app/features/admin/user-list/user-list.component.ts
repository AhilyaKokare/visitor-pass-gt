// FILE: frontend/visitor-pass-frontend/src/app/features/admin/user-list/user-list.component.ts

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
import { Page } from '../../../core/models/page.model';
import { PaginationComponent } from '../../../shared/pagination/pagination.component';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, RouterModule, TitleCasePipe, LoadingSpinnerComponent, FormsModule, PaginationComponent],
  templateUrl: './user-list.component.html',
})
export class UserListComponent implements OnInit {

  @ViewChild('closeModalButton') closeModalButton!: ElementRef;

  // This is the single source of truth for the list
  userPage: Page<User> | null = null;

  tenantId!: number;
  isLoading = true;
  isSubmitting = false;
  currentPage = 0;
  pageSize = 10;
  newUser: any = { role: 'ROLE_EMPLOYEE' };

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private toastr: ToastrService,
    private confirmationService: ConfirmationService,
    private zone: NgZone
  ) {}

  ngOnInit(): void {
    const user = this.authService.getDecodedToken();
    if (user && user.tenantId) {
      this.tenantId = user.tenantId;
      this.loadUsers();
    }
  }

  loadUsers(): void {
    this.isLoading = true;
    this.userService.getUsers(this.tenantId, this.currentPage, this.pageSize).subscribe({
      next: data => {
        this.userPage = data;
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
    // ... (Your existing onCreateUser logic. Ensure it calls this.loadUsers() on success)
    this.isSubmitting = true;
    this.userService.createUser(this.tenantId, this.newUser).subscribe({
      next: () => {
        this.toastr.success('User created successfully!');
        this.loadUsers(); 
        this.closeModalButton.nativeElement.click(); 
        this.newUser = { role: 'ROLE_EMPLOYEE' }; 
        this.isSubmitting = false;
      },
      error: (err) => {
        this.toastr.error(err.error.message || 'Failed to create user.');
        this.isSubmitting = false;
      }
    });
  }

  toggleUserStatus(userToToggle: User): void {
    const action = userToToggle.isActive ? 'Deactivate' : 'Activate';
    const newStatus = !userToToggle.isActive;

    if (this.confirmationService.confirm(`Are you sure you want to ${action} user "${userToToggle.name}"?`)) {
      this.userService.updateUserStatus(this.tenantId, userToToggle.id, newStatus).subscribe({
        next: (updatedUserFromServer) => {
          this.zone.run(() => {
            this.toastr.success(`User has been ${action.toLowerCase()}d.`);
            
            // VVV --- THIS IS THE FIX --- VVV
            // The logic now correctly checks and updates the userPage.content array
            if (this.userPage && this.userPage.content) {
              const updatedContent = this.userPage.content.map(u => 
                u.id === updatedUserFromServer.id ? updatedUserFromServer : u
              );
              this.userPage = { ...this.userPage, content: updatedContent };
            }
            // ^^^ --- END OF FIX --- ^^^
          });
        },
        error: (err) => {
          this.toastr.error(err?.error?.message || 'Failed to update user status.');
        }
      });
    }
  }

  // ... (Your other helper/validation methods can go here if you have them)
}