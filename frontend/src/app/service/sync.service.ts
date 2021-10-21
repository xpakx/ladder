import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { DateRequest } from '../entity/date-request';
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
}
