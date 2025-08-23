import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const expectedRoles = route.data['expectedRoles'] as Array<string>;
  const user = authService.getDecodedToken();

  if (!authService.isLoggedIn() || !user || !expectedRoles.includes(user.role)) {
    router.navigate(['/home']); // Redirect to a safe default page
    return false;
  }

  return true;
};
