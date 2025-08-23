import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-user-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './user-create.component.html',
})
export class UserCreateComponent {
  newUser: any = { role: 'ROLE_EMPLOYEE' };
  tenantId!: number;
  isSubmitting = false;

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private toastr: ToastrService,
    private router: Router
  ) {
    const user = this.authService.getDecodedToken();
    if (user && user.tenantId) {
      this.tenantId = user.tenantId;
    }
  }

  onCreateUser(): void {
    this.isSubmitting = true;
    this.userService.createUser(this.tenantId, this.newUser).subscribe({
      next: (response) => {
        console.log('User created successfully:', response);
        this.toastr.success('User created successfully!');
        this.isSubmitting = false;

        // Use setTimeout to ensure the success message is shown before navigation
        setTimeout(() => {
          this.router.navigate(['/tenant-admin/users']);
        }, 100);
      },
      error: (err) => {
        console.error('Error creating user:', err);
        this.toastr.error(err.error?.message || 'Failed to create user.');
        this.isSubmitting = false;
      }
    });
  }
}
