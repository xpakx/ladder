import { HttpClient } from '@angular/common/http';
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

  public sendProjectsAsCSV(file: File): Observable<any> {   
    let userId  = this.getUserId();
    let formData: FormData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiServerUrl}/${userId}/import/csv/projects`, formData);
  }

  public sendTasksAsCSV(file: File): Observable<any> {   
    let userId  = this.getUserId();
    let formData: FormData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiServerUrl}/${userId}/import/csv/tasks`, formData);
  }

  public sendProjectTasksAsCSV(file: File, projectId: number): Observable<any> {   
    let userId  = this.getUserId();
    let formData: FormData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiServerUrl}/${userId}/import/csv/projects/${projectId}/tasks`, formData);
  }
}
