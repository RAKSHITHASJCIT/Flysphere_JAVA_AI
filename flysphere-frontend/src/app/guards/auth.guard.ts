import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  // ✅ Check for stored user instead of token (JWT not implemented)
  const user = localStorage.getItem('user');

  if (user) {
    return true;
  } else {
    router.navigate(['/login']);
    return false;
  }
};
