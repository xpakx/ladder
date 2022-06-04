import { HttpClient, HttpHeaders } from '@angular/common/http';
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

  private getHeaders(): HttpHeaders {
    let token = localStorage.getItem("token");
    return new HttpHeaders({'Authorization':`Bearer ${token}`});
  }

  public getProjectTasksAsCSV(projectId: number): Observable<Blob> {   
    let userId  = this.getUserId();
    return this.http.get(`${this.apiServerUrl}/${userId}/export/csv/projects/${projectId}/tasks`, { responseType: 'blob', headers: this.getHeaders() });
  }

  public getProjectsAsCSV(): Observable<Blob> {   
    let userId  = this.getUserId();
    return this.http.get(`${this.apiServerUrl}/${userId}/export/csv/projects`, { responseType: 'blob', headers: this.getHeaders() });
  }

  public getTasksAsCSV(): Observable<Blob> {   
    let userId  = this.getUserId();
    return this.http.get(`${this.apiServerUrl}/${userId}/export/csv/tasks`, { responseType: 'blob', headers: this.getHeaders() });
  }

  public getProjectTasksAsTXT(projectId: number): Observable<Blob> {   
    let userId  = this.getUserId();
    return this.http.get(`${this.apiServerUrl}/${userId}/export/txt/projects/${projectId}/tasks`, { responseType: 'blob', headers: this.getHeaders() });
  }

  public getProjectsAsTXT(): Observable<Blob> {   
    let userId  = this.getUserId();
    return this.http.get(`${this.apiServerUrl}/${userId}/export/txt/projects`, { responseType: 'blob', headers: this.getHeaders() });
  }

  public getTasksAsTXT(): Observable<Blob> {   
    let userId  = this.getUserId();
    return this.http.get(`${this.apiServerUrl}/${userId}/export/txt/tasks`, { responseType: 'blob', headers: this.getHeaders() });
  }
}
