import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
// import * as SockJS from 'sockjs-client';
import { Subject } from 'rxjs';
import { environment } from '../../../environments/environment';


import SockJS from 'sockjs-client';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private stompClient: Client;
  private isConnected = false;
  
  // Use a Subject to broadcast messages to any component that subscribes
  public dashboardUpdate$: Subject<any> = new Subject<any>();

  constructor() {
    this.stompClient = new Client({
        
      // The backend endpoint we configured in WebSocketConfig.java
      webSocketFactory: () => new SockJS(`${environment.apiUrl.replace('/api', '')}/ws`),
      reconnectDelay: 5000,
      debug: (str) => {
        // You can enable this for detailed WebSocket logging in the browser console
        // console.log(new Date(), str);
      },
    });
  }

  public connect(tenantId: number): void {
    if (this.isConnected) {
      return;
    }

    this.stompClient.onConnect = (frame) => {
      this.isConnected = true;
      console.log('WebSocket Connected: ' + frame);

      // Subscribe to the tenant-specific dashboard topic
      this.stompClient.subscribe(`/topic/dashboard/${tenantId}`, (message: IMessage) => {
        // When a message arrives, parse it and push it to our Subject
        this.dashboardUpdate$.next(JSON.parse(message.body));
      });
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    this.stompClient.activate();
  }

  public disconnect(): void {
    if (this.isConnected) {
      this.stompClient.deactivate();
      this.isConnected = false;
      console.log('WebSocket Disconnected');
    }
  }
}