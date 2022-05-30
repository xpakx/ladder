import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { CollabTaskDetails } from '../task/dto/collab-task-details';
import { SyncData } from 'src/app/sync/dto/sync-data';
import { SyncService } from './sync.service';
import { TreeService } from 'src/app/utils/tree.service';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private url = environment.notificationServerUrl;

  constructor(private tree: TreeService, private service: SyncService) { }

  private getUserId(): string | null {
    return localStorage.getItem("user_id");
  }

  subscribe(): void {
    let id = this.getUserId();
    if(id) {
      let eventSource = new EventSource(`${this.url}/subscription/${id}`);
      eventSource.onopen = (e) => console.log("open");

      eventSource.onerror = (e) => {
          console.log(e);
      };

      eventSource.addEventListener("message", (event) => {
        this.onNotificationSent(event);
      }, false);
    }
  }

  onNotificationSent(event: MessageEvent<any>): void {
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
    } else if(type == 'DELETE_HABIT') {
      setTimeout(() => this.deleteHabit(JSON.parse(event.data).id), 500);
    } else if(type == 'DELETE_FILTER') {
      setTimeout(() => this.deleteFilter(JSON.parse(event.data).id), 500);
    } else if(type == 'DELETE_CPROJ') {
      setTimeout(() => this.deleteCProject(JSON.parse(event.data).id), 500);
    } else if(type == 'DELETE_CTASK') {
      setTimeout(() => this.deleteCTask(JSON.parse(event.data).id), 500);
    }
  }

  deleteProject(id: number): void {
    let proj = this.tree.getProjectById(id);
    if(proj) {
      this.tree.deleteProject(id);
    }
  }

  deleteCProject(id: number): void {
    let proj = this.tree.getCollabProjectById(id);
    if(proj) {
      this.tree.deleteCollabProject(id);
    }
  }

  deleteLabel(id: number): void {
    let label = this.tree.getLabelById(id);
    if(label) {
      this.tree.deleteLabel(id);
    }
  }

  deleteTask(id: number): void {
    let task = this.tree.getTaskById(id);
    if(task) {
      this.tree.deleteTask(id);
    }
  }

  deleteCTask(id: number): void {
    let task = this.tree.getCollabTaskById(id);
    if(task) {
      this.tree.deleteCollabTask(id);
    }
  }

  deleteHabit(id: number): void {
    let habit = this.tree.getHabitById(id);
    if(habit) {
      this.tree.deleteHabit(id);
    }
  }

  deleteFilter(id: number): void {
    let filter = this.tree.getFilterById(id);
    if(filter) {
      this.tree.deleteFilter(id);
    }
  }

  testSync(timestamp: Date): void {
    let maxDate: Date = new Date(0);
    let dates: Date[] = this.tree.getProjects().map((a) => a.modifiedAt);
    dates = dates.concat(
      this.tree.getTasks().map((a) => a.modifiedAt)
    );
    dates = dates.concat(
      this.tree.getLabels().map((a) => a.modifiedAt)
    );
    dates = dates.concat(
      this.tree.getHabits().map((a) => a.modifiedAt)
    );
    dates = dates.concat(
      this.tree.getCompletions().map((a) => a.date)
    );
    dates = dates.concat(
      this.tree.getFilters().map((a) => a.modifiedAt)
    );
    let projectArchiv = this.tree.getLastProjectArchivization();
    if(projectArchiv) {
      dates.push(projectArchiv)
    }
    let taskArchiv = this.tree.getLastTaskArchivization();
    if(taskArchiv) {
      dates.push(taskArchiv)
    }
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

  sync(time: Date): void {
    let date = new Date(time);
    let diff = (date.getTimezoneOffset() / 60) * -1; 
    date.setTime(date.getTime() + (diff * 60) * 60 * 1000);
    console.log(date.toISOString());
    this.service.sync({'date': date}).subscribe(
      (response: SyncData) => {
        let ids: number[] = this.tree.filterNewCollabsIds(response.collabs.map((a) => a.project));
        this.tree.sync(response);
        this.syncNewCollabs(ids)
      },
      (error: HttpErrorResponse) => {}
    );
  }

  syncNewCollabs(ids: number[]): void {
    if(ids.length == 0) {return;}
    this.service.syncCollabTasks({'ids': ids}).subscribe(
      (response: CollabTaskDetails[]) => {
        this.tree.syncCollabTasks(response);
      },
      (error: HttpErrorResponse) => {}
    );
  }
}
