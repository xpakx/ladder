import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AddTaskRequest } from './dto/add-task-request';
import { BooleanRequest } from '../common/dto/boolean-request';
import { DateRequest } from '../common/dto/date-request';
import { IdRequest } from '../common/dto/id-request';
import { PriorityRequest } from '../common/dto/priority-request';
import { Task } from './dto/task';
import { MultilevelMovableService } from '../common/multilevel-movable-service';

@Injectable({
  providedIn: 'root'
})
export class CollabTaskService implements MultilevelMovableService<Task> {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  private getUserId() {
    return localStorage.getItem("user_id");
  }

  public completeTask(taskId: number, request: BooleanRequest):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/collab/tasks/${taskId}/completed`, request);
  }

  updateTask(request: AddTaskRequest, taskId: number): Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/collab/tasks/${taskId}`, request);
  }

  public moveAfter(request: IdRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/collab/tasks/${taskId}/move/after`, request);
  }

  public moveAsChild(request: IdRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/collab/tasks/${taskId}/move/asChild`, request);
  }

  public moveAsFirst(taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/collab/tasks/${taskId}/move/asFirst`, null);
  }

  public updateCollapse(taskId: number, request: BooleanRequest):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/collab/tasks/${taskId}/collapse`, request);
  }

  public deleteTask(taskId: number):  Observable<any> {
    let userId  = this.getUserId();
    return this.http.delete<any>(`${this.apiServerUrl}/${userId}/collab/tasks/${taskId}`);
  }

  public updateTaskDueDate(request: DateRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/collab/tasks/${taskId}/due`, request);
  }

  public addTaskAfter(request: AddTaskRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.post<Task>(`${this.apiServerUrl}/${userId}/collab/tasks/${taskId}/after`, request);
  }

  public addTaskBefore(request: AddTaskRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.post<Task>(`${this.apiServerUrl}/${userId}/collab/tasks/${taskId}/before`, request);
  }

  public addTaskAsChild(request: AddTaskRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.post<Task>(`${this.apiServerUrl}/${userId}/collab/tasks/${taskId}/children`, request);
  }

  updateTaskPriority(request: PriorityRequest, taskId: number): Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/collab/tasks/${taskId}/priority`, request);
  }


}
