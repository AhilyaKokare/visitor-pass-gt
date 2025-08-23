import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-main-dashboard',
  standalone: true,
  templateUrl: './main-dashboard.component.html',
})
export class MainDashboardComponent implements OnInit {
  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    const user = this.authService.getDecodedToken();
    if (user && user.role) {
      this.redirectToRoleDashboard(user.role);
    } else {
      this.authService.logout();
    }
  }

  private redirectToRoleDashboard(role: string): void {
    const roleRoutes: { [key: string]: string } = {
      'ROLE_SUPER_ADMIN': '/super-admin/dashboard',
      'ROLE_TENANT_ADMIN': '/tenant-admin/users',
      'ROLE_EMPLOYEE': '/passes/create',
      'ROLE_APPROVER': '/passes/approve',
      'ROLE_SECURITY': '/security/dashboard',
    };
    const route = roleRoutes[role] || '/home';
    this.router.navigate([route]);
  }
}
