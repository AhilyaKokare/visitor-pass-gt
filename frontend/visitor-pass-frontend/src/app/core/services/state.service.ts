import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

/**
 * A shared service for managing and broadcasting application-wide state changes.
 * This helps to keep different components in sync without them needing to know about each other.
 */
@Injectable({
  providedIn: 'root'
})
export class StateService {

  // A Subject is used to send out notifications to multiple listeners (observers).
  // We use <void> because we don't need to send any data, just a "ping" to signal a change.
  private passChangeSubject = new Subject<void>();

  /**
   * An observable that components can subscribe to, to be notified of any changes
   * to visitor pass data (e.g., creation, approval, check-in).
   */
  passChange$ = this.passChangeSubject.asObservable();

  /**
   * This method is called by any component after it successfully completes an action
   * that modifies visitor pass data on the backend. It broadcasts the change notification.
   */
  notifyPassChange(): void {
    console.log('StateService: Notifying all listeners of a pass change.');
    this.passChangeSubject.next();
  }
}
