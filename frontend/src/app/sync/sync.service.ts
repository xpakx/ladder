import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { CollabTaskDetails } from '../task/dto/collab-task-details';
import { DateRequest } from './dto/notification-date-request';
import { IdCollectionRequest } from '../common/dto/id-collection-request';
import { SyncData } from 'src/app/sync/dto/sync-data';

@Injectable({
  providedIn: 'root'
})
export class SyncService {
  private url = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  private getUserId(): string | null {
    return localStorage.getItem("user_id");
  }

  private getHeaders(): HttpHeaders {
    let token = localStorage.getItem("token");
    return new HttpHeaders({'Authorization':`Bearer ${token}`});
  }

  public sync(request: DateRequest):  Observable<SyncData> {
    let userId  = this.getUserId();
    return this.http.post<SyncData>(`${this.url}/${userId}/sync`, request, { headers: this.getHeaders() });
  }
  
  public syncCollabTasks(request: IdCollectionRequest):  Observable<CollabTaskDetails[]> {
    let userId  = this.getUserId();
    return this.http.post<CollabTaskDetails[]>(`${this.url}/${userId}/sync/collab/tasks`, request, { headers: this.getHeaders() });
  }
}
