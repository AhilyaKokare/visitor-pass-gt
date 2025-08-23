import { HttpErrorResponse, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { inject } from '@angular/core';
import { Router } from '@angular/router';

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const router = inject(Router);
  const token = localStorage.getItem('token');

  // Skip authentication for password reset endpoints
  const isPasswordResetEndpoint = req.url.includes('/forgot-password') || req.url.includes('/reset-password');
  const isLoginEndpoint = req.url.includes('/login');

  if (token && !isPasswordResetEndpoint && !isLoginEndpoint) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error('HTTP Error:', error.status, error.message, 'URL:', req.url);
      if (error.status === 401 || error.status === 403) {
        console.log('Authentication error, redirecting to login');
        localStorage.removeItem('token');
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
