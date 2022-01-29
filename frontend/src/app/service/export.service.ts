import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ExportService {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  private getUserId() {
    return localStorage.getItem("user_id");
  }

  public getProjectTasksAsCSV(projectId: number): Observable<Blob> {   
    let userId  = this.getUserId();
    return this.http.get(`${this.apiServerUrl}/${userId}/export/csv/projects/${projectId}/tasks`, { responseType: 'blob' });
  }

  public getProjectsAsCSV(): Observable<Blob> {   
    let userId  = this.getUserId();
    return this.http.get(`${this.apiServerUrl}/${userId}/export/csv/projects`, { responseType: 'blob' });
  }

  public getTasksAsCSV(): Observable<Blob> {   
    let userId  = this.getUserId();
    return this.http.get(`${this.apiServerUrl}/${userId}/export/csv/tasks`, { responseType: 'blob' });
  }
}
