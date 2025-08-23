import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { ProfileService } from '../../../core/services/profile.service';
import { LoadingSpinnerComponent } from '../../../shared/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-edit-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, LoadingSpinnerComponent],
  templateUrl: './edit-profile.component.html'
})
export class EditProfileComponent implements OnInit {
  editableProfile: any = null;
  isSubmitting = false;

  constructor(
    private profileService: ProfileService,
    private toastr: ToastrService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.profileService.getMyProfile().subscribe(data => {
      this.editableProfile = {
        email: data.email,
        contact: data.contact,
        address: data.address
      };
    });
  }

  onUpdateProfile(): void {
    this.isSubmitting = true;
    this.profileService.updateMyProfile(this.editableProfile).subscribe({
      next: () => {
        this.toastr.success('Profile updated successfully!');
        this.router.navigate(['/profile']);
        this.isSubmitting = false;
      },
      error: () => {
        this.toastr.error('Failed to update profile.');
        this.isSubmitting = false;
      }
    });
  }
}
