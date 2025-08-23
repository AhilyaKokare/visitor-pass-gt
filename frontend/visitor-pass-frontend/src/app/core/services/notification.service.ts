import { Injectable } from '@angular/core';
import { ToastrService } from 'ngx-toastr';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  constructor(private toastr: ToastrService) { }

  showSuccess(message: string, title: string = 'Success'): void {
    this.toastr.success(message, title);
  }

  showError(message: string, title: string = 'Error'): void {
    this.toastr.error(message, title);
  }
}
