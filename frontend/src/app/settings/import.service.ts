import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ImportService {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  private getUserId() {
    return localStorage.getItem("user_id");
  }

  private getHeaders(): HttpHeaders {
    let token = localStorage.getItem("token");
    return new HttpHeaders({'Authorization':`Bearer ${token}`});
  }

  public sendProjectsAsCSV(file: File): Observable<any> {   
    let userId  = this.getUserId();
    let formData: FormData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiServerUrl}/${userId}/import/csv/projects`, formData, { headers: this.getHeaders() });
  }

  public sendTasksAsCSV(file: File): Observable<any> {   
    let userId  = this.getUserId();
    let formData: FormData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiServerUrl}/${userId}/import/csv/tasks`, formData, { headers: this.getHeaders() });
  }

  public sendProjectTasksAsCSV(file: File, projectId: number): Observable<any> {   
    let userId  = this.getUserId();
    let formData: FormData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiServerUrl}/${userId}/import/csv/projects/${projectId}/tasks`, formData, { headers: this.getHeaders() });
  }
}
