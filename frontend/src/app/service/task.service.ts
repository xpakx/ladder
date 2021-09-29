import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AddTaskRequest } from '../entity/add-task-request';
import { BooleanRequest } from '../entity/boolean-request';
import { DateRequest } from '../entity/date-request';
import { IdRequest } from '../entity/id-request';
import { Task } from '../entity/task';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  private getUserId() {
    return localStorage.getItem("user_id");
  }

  public completeTask(taskId: number, request: BooleanRequest):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/completed`, request);
  }

  updateTask(request: AddTaskRequest, taskId: number): Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}`, request);
  }

  public moveTaskAfter(request: IdRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/move/after`, request);
  }

  public moveTaskAsChild(request: IdRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/move/asChild`, request);
  }

  public updateTaskCollapse(taskId: number, request: BooleanRequest):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/collapse`, request);
  }

  public deleteTask(taskId: number):  Observable<any> {
    let userId  = this.getUserId();
    return this.http.delete<any>(`${this.apiServerUrl}/${userId}/tasks/${taskId}`);
  }

  public updateTaskDueDate(request: DateRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/due`, request);
  }

  public addTaskAfter(request: AddTaskRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.post<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/after`, request);
  }

  public addTaskBefore(request: AddTaskRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.post<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/before`, request);
  }
}
