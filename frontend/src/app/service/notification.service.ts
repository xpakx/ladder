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
      setTimeout(() => this.deleteProject(JSON.parse(event.data).id), 500);
    } else if(type == 'DELETE_LABEL') {
      setTimeout(() => this.deleteLabel(JSON.parse(event.data).id), 500);
    } else if(type == 'DELETE_TASK') {
      setTimeout(() => this.deleteTask(JSON.parse(event.data).id), 500);
    }
  }

  deleteProject(id: number) {
    let proj = this.tree.getProjectById(id);
    if(proj) {
      this.tree.deleteProject(id);
    }
  }

  deleteLabel(id: number) {
    let label = this.tree.getLabelById(id);
    if(label) {
      this.tree.deleteLabel(id);
    }
  }

  deleteTask(id: number) {
    let task = this.tree.getTaskById(id);
    if(task) {
      this.tree.deleteTask(id);
    }
  }

  testSync(timestamp: Date) {
    let maxDate: Date = new Date(0);
    let dates: Date[] = this.tree.getProjects().map((a) => a.modifiedAt);
    dates = dates.concat(
      this.tree.getTasks().map((a) => a.modifiedAt)
    );
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
    let date = new Date(time);
    let diff = (date.getTimezoneOffset() / 60) * -1; 
    date.setTime(date.getTime() + (diff * 60) * 60 * 1000);
    console.log(date.toISOString());
    this.service.sync({'date': date}).subscribe(
      (response: SyncData) => {
        this.tree.sync(response);
      },
      (error: HttpErrorResponse) => {}
    );
  }
}
