import { Component, OnInit } from '@angular/core';
import { ProfileService } from '../../../core/services/profile.service';
import { User } from '../../../core/models/user.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-my-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './my-profile.component.html'
})
export class MyProfileComponent implements OnInit {
  profile?: User;
  editableProfile: any = {};

  constructor(private profileService: ProfileService, private toastr: ToastrService) {}

  ngOnInit(): void {
    this.profileService.getMyProfile().subscribe(data => {
      this.profile = data;
      this.editableProfile = {
        email: data.email,
        contact: data.contact,
        address: data.address
      };
    });
  }

  onUpdateProfile(): void {
    this.profileService.updateMyProfile(this.editableProfile).subscribe({
      next: (updatedProfile) => {
        this.profile = updatedProfile;
        this.toastr.success('Profile updated successfully!');
      },
      error: (error: any) => {
        console.error('Profile update failed:', error);

        let errorMessage = 'Failed to update profile.';

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
        } else if (error.error?.message) {
          errorMessage = error.error.message;
        }

        this.toastr.error(errorMessage, 'Profile Update Failed', { timeOut: 5000 });
      }
    });
  }

  isSuperAdminOrTenantAdmin(): boolean {
    return this.profile?.role === 'ROLE_SUPER_ADMIN' || this.profile?.role === 'ROLE_TENANT_ADMIN';
  }
}
