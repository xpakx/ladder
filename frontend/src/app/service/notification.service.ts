import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { environment } from 'src/environments/environment';
import { SyncData } from '../entity/sync-data';
import { SyncService } from './sync.service';
import { TreeService } from './tree.service';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private url = environment.notificationServerUrl;

  constructor(private tree: TreeService, private service: SyncService) { }

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
    let timestamp = new Date(JSON.parse(event.data).time);
    let type: string = JSON.parse(event.data).type;

    if(type == 'UPDATE') {
      setTimeout(() => this.testSync(timestamp), 500);
    } else if(type == 'DELETE_PROJ') {
      setTimeout(() => this.delete(JSON.parse(event.data).id), 500);
    }
  }

  delete(id: number) {
    let proj = this.tree.getProjectById(id);
    if(proj) {
      this.tree.deleteProject(id);
    }
  }

  testSync(timestamp: Date) {
    let maxDate: Date = new Date(0);
    let dates: Date[] = this.tree.getProjects().map((a) => a.modifiedAt);
    for(let date of dates) {
      if(date > maxDate) {
        maxDate = date;
      }
    }
    if(maxDate < timestamp) {
      console.log("Sync " + maxDate);
      this.sync(maxDate);
    } else {
      console.log("Already synced " + maxDate);
    }  
  }

  sync(time: Date) {
    this.service.sync({'date': new Date(time)}).subscribe(
      (response: SyncData) => {
        this.tree.sync(response);
      },
      (error: HttpErrorResponse) => {}
    );
  }
}
