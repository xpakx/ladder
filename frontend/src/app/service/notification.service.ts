import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { environment } from 'src/environments/environment';
import { TreeService } from './tree.service';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private url = environment.notificationServerUrl;

  constructor(private tree: TreeService) { }

  subscribe() {
    let eventSource = new EventSource(`${this.url}/subscription/1`);
    eventSource.onopen = (e) => console.log("open");

    eventSource.onerror = (e) => {
        console.log(e);
    };

    eventSource.addEventListener("message", (event) => {
      this.onNotificationSent(event);
    }, false);
  }

  onNotificationSent(event: MessageEvent<any>) {
    console.log("Got an event" + event.data);
    let maxDate: Date = new Date(0);
    for(let date of this.tree.getProjects().map((a) => a.modifiedAt)) {
      if(date > maxDate) {maxDate = date;}
    }
    if(maxDate > new Date(event.data.time)) {
      console.log("Sync");
    } else {
      console.log("Already synced");
    }  
  }
}
