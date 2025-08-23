import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ConfirmationService {
  confirm(message: string = 'Are you sure?'): boolean {
    return window.confirm(message);
  }
}
