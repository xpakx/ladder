import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { CollabTaskDetails } from '../entity/collab-task-details';
import { DateRequest } from '../entity/date-request';
import { IdCollectionRequest } from '../entity/id-collection-request';
import { SyncData } from '../entity/sync-data';

@Injectable({
  providedIn: 'root'
})
export class SyncService {
  private url = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  private getUserId() {
    return localStorage.getItem("user_id");
  }

  public sync(request: DateRequest):  Observable<SyncData> {
    let userId  = this.getUserId();
    return this.http.post<SyncData>(`${this.url}/${userId}/sync`, request);
  }
  
  public syncCollabTasks(request: IdCollectionRequest):  Observable<CollabTaskDetails[]> {
    let userId  = this.getUserId();
    return this.http.post<CollabTaskDetails[]>(`${this.url}/${userId}/sync/collab/tasks`, request);
  }
}
