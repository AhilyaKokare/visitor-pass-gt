import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { Observable } from 'rxjs'; // <-- IMPORT OBSERVABLE

// Define a simple interface for the decoded token's shape
interface DecodedToken {
  sub: string;
  role: string;
}

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
   templateUrl: './navbar.component.html'
})
export class NavbarComponent {
  // Give the observable a proper type
  currentUser$: Observable<DecodedToken | null>;

  constructor(private authService: AuthService) {
    // Initialize in the constructor
    this.currentUser$ = this.authService.getCurrentUser();
  }

  logout(): void {
    this.authService.logout();
  }
}
