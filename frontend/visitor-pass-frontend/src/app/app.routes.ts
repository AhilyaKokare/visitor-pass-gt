import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { ForgotPasswordComponent } from './features/auth/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './features/auth/reset-password/reset-password.component';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { MainDashboardComponent } from './features/dashboard/main-dashboard/main-dashboard.component';
import { SuperAdminDashboardComponent } from './features/admin/super-admin-dashboard/super-admin-dashboard.component';
import { UserListComponent } from './features/admin/user-list/user-list.component';
import { ViewProfileComponent } from './features/profile/view-profile/view-profile.component';
import { EditProfileComponent } from './features/profile/edit-profile/edit-profile.component';
import { CreatePassComponent } from './features/passes/create-pass/create-pass.component';
import { MyPassHistoryComponent } from './features/passes/my-pass-history/my-pass-history.component';
import { ApprovalQueueComponent } from './features/passes/approval-queue/approval-queue.component';
import { SecurityDashboardComponent } from './features/security/security-dashboard/security-dashboard.component';
import { HomeComponent } from './features/dashboard/home/home.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password/:token', component: ResetPasswordComponent },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'home', component: HomeComponent },
      { path: 'dashboard', component: MainDashboardComponent },
      { path: 'profile', component: ViewProfileComponent },
      { path: 'profile/edit', component: EditProfileComponent },

      // Super Admin
      { path: 'super-admin/dashboard', component: SuperAdminDashboardComponent, canActivate: [roleGuard], data: { expectedRoles: ['ROLE_SUPER_ADMIN'] } },

      // Tenant Admin
      { path: 'tenant-admin/dashboard', component: UserListComponent, canActivate: [roleGuard], data: { expectedRoles: ['ROLE_TENANT_ADMIN'] } },
      { path: 'tenant-admin/users', component: UserListComponent, canActivate: [roleGuard], data: { expectedRoles: ['ROLE_TENANT_ADMIN'] } },
      // Employee
      { path: 'passes/create', component: CreatePassComponent, canActivate: [roleGuard], data: { expectedRoles: ['ROLE_EMPLOYEE', 'ROLE_TENANT_ADMIN'] } },
     { path: 'passes/my-pass-history', component: MyPassHistoryComponent, canActivate: [roleGuard], data: { expectedRoles: ['ROLE_EMPLOYEE', 'ROLE_TENANT_ADMIN'] } },
      // Approver
      { path: 'passes/approve', component: ApprovalQueueComponent, canActivate: [roleGuard], data: { expectedRoles: ['ROLE_APPROVER', 'ROLE_TENANT_ADMIN'] } },

      // Security
      { path: 'security/dashboard', component: SecurityDashboardComponent, canActivate: [roleGuard], data: { expectedRoles: ['ROLE_SECURITY', 'ROLE_TENANT_ADMIN'] } },

      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
