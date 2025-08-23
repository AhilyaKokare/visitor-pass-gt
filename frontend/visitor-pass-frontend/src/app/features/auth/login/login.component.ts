import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  // VVV THIS IS THE FIX VVV
  // The backend expects a field named 'username', which will hold the user's email.
  credentials = { username: '', password: '' };
  
  isLoggingIn = false;

  constructor(
    private authService: AuthService, 
    private router: Router,
    private toastr: ToastrService
  ) {}

  onLogin(): void {
    // Note: The credentials object now has 'username' but the form validation is for email.
    // This is handled in the HTML.
    if (!this.credentials.username || !this.credentials.password) {
      this.toastr.error('Please enter both email and password.');
      return;
    }

    this.isLoggingIn = true;
    this.authService.login(this.credentials).subscribe({
      next: () => {
        this.router.navigate(['/home']);
      },
      error: (err) => {
        this.toastr.error('Login failed. Please check your credentials and try again.');
        this.isLoggingIn = false;
      }
    });
  }
}