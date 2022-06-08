import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AddTaskRequest } from './dto/add-task-request';
import { BooleanRequest } from '../common/dto/boolean-request';
import { DateRequest } from '../common/dto/date-request';
import { IdCollectionRequest } from '../common/dto/id-collection-request';
import { IdRequest } from '../common/dto/id-request';
import { PriorityRequest } from '../common/dto/priority-request';
import { Task } from './dto/task';
import { TaskDetails } from './dto/task-details';
import { MultilevelMovableService } from '../common/multilevel-movable-service';

@Injectable({
  providedIn: 'root'
})
export class TaskService implements MultilevelMovableService<Task> {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  private getUserId(): string | null {
    return localStorage.getItem("user_id");
  }

  transformDate(time: Date): Date {
    time.setTime(time.getTime() + time.getTimezoneOffset()*60*1000*(-1));
    return time;
  }

  private getHeaders(): HttpHeaders {
    let token = localStorage.getItem("token");
    return new HttpHeaders({'Authorization':`Bearer ${token}`});
  }

  public completeTask(taskId: number, request: BooleanRequest):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/completed`, request, { headers: this.getHeaders() });
  }

  updateTask(request: AddTaskRequest, taskId: number): Observable<Task> {
    let userId  = this.getUserId();
    request.due = request.due ? this.transformDate(request.due) : null;
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}`, request, { headers: this.getHeaders() });
  }

  public moveAfter(request: IdRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/move/after`, request, { headers: this.getHeaders() });
  }

  public moveAsChild(request: IdRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/move/asChild`, request, { headers: this.getHeaders() });
  }

  public moveAsFirst(taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/move/asFirst`, null, { headers: this.getHeaders() });
  }

  public updateCollapse(taskId: number, request: BooleanRequest):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/collapse`, request, { headers: this.getHeaders() });
  }

  public deleteTask(taskId: number):  Observable<any> {
    let userId  = this.getUserId();
    return this.http.delete<any>(`${this.apiServerUrl}/${userId}/tasks/${taskId}`, { headers: this.getHeaders() });
  }

  public updateTaskDueDate(request: DateRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    request.date = request.date ? this.transformDate(request.date) : undefined;
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/due`, request, { headers: this.getHeaders() });
  }

  public addTaskAfter(request: AddTaskRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    request.due = request.due ? this.transformDate(request.due) : null;
    return this.http.post<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/after`, request, { headers: this.getHeaders() });
  }

  public addTaskBefore(request: AddTaskRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    request.due = request.due ? this.transformDate(request.due) : null;
    return this.http.post<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/before`, request, { headers: this.getHeaders() });
  }

  public addTaskAsChild(request: AddTaskRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    request.due = request.due ? this.transformDate(request.due) : null;
    return this.http.post<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/children`, request, { headers: this.getHeaders() });
  }

  updateTaskProject(request: IdRequest, taskId: number): Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/project`, request, { headers: this.getHeaders() });
  }

  updateTaskPriority(request: PriorityRequest, taskId: number): Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/priority`, request, { headers: this.getHeaders() });
  }

  public moveAfterDaily(request: IdRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/daily/move/after`, request, { headers: this.getHeaders() });
  }

  public moveAsFirstDaily(taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/daily/move/asFirst`, null, { headers: this.getHeaders() });
  }

  public moveAsFirstWithDate(taskId: number, request: DateRequest):  Observable<Task> {
    let userId  = this.getUserId();
    request.date = request.date ? this.transformDate(request.date) : undefined;
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/daily/move/asFirstWithDate`, request, { headers: this.getHeaders() });
  }


  public duplicateTask(taskId: number):  Observable<TaskDetails[]> {
    let userId  = this.getUserId();
    return this.http.post<TaskDetails[]>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/duplicate`, null, { headers: this.getHeaders() });
  }

  updateTaskLabels(request: IdCollectionRequest, taskId: number): Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/labels`, request, { headers: this.getHeaders() });
  }

  public getTaskForProject(projectId: number):  Observable<TaskDetails[]> {
    let userId  = this.getUserId();
    return this.http.get<TaskDetails[]>(`${this.apiServerUrl}/${userId}/projects/${projectId}/tasks`, { headers: this.getHeaders() });
  }

  archiveTask(taskId: number, request: BooleanRequest):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/archive`, request, { headers: this.getHeaders() });
  }

  public getArchivedTasks(projectId: number):  Observable<TaskDetails[]> {
    let userId  = this.getUserId();
    return this.http.get<TaskDetails[]>(`${this.apiServerUrl}/${userId}/projects/${projectId}/tasks/archived`, { headers: this.getHeaders() });
  }

  public rescheduleOverdueTasks(request: DateRequest):  Observable<Task[]> {
    let userId  = this.getUserId();
    request.date = request.date ? this.transformDate(request.date) : undefined;
    return this.http.put<Task[]>(`${this.apiServerUrl}/${userId}/tasks/overdue/due`, request, { headers: this.getHeaders() });
  }

  updateAssigned(request: IdRequest, taskId: number): Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/assigned`, request, { headers: this.getHeaders() });
  }
}
