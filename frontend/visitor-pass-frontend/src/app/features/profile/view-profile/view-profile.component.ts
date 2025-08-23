import { Component, OnInit } from '@angular/core';
import { CommonModule, TitleCasePipe, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ProfileService } from '../../../core/services/profile.service';
import { User } from '../../../core/models/user.model';
import { LoadingSpinnerComponent } from '../../../shared/loading-spinner/loading-spinner.component';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-view-profile',
  standalone: true,
  imports: [CommonModule, RouterModule, LoadingSpinnerComponent, TitleCasePipe, DatePipe],
  templateUrl: './view-profile.component.html'
})
export class ViewProfileComponent implements OnInit {
  profile: User | null = null;
  isLoading = true;

  constructor(private profileService: ProfileService, private toastr: ToastrService) {}

  ngOnInit(): void {
    this.profileService.getMyProfile().subscribe({
      next: data => {
        this.profile = data;
        this.isLoading = false;
      },
      error: () => {
        this.toastr.error('Failed to load profile data.');
        this.isLoading = false;
      }
    });
  }

  isSuperAdmin(): boolean {
    return this.profile?.role === 'ROLE_SUPER_ADMIN';
  }

  isSuperAdminOrTenantAdmin(): boolean {
    return this.profile?.role === 'ROLE_SUPER_ADMIN' || this.profile?.role === 'ROLE_TENANT_ADMIN';
  }
}
