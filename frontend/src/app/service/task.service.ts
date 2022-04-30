import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AddTaskRequest } from '../entity/add-task-request';
import { BooleanRequest } from '../entity/boolean-request';
import { DateRequest } from '../entity/date-request';
import { IdCollectionRequest } from '../entity/id-collection-request';
import { IdRequest } from '../entity/id-request';
import { PriorityRequest } from '../entity/priority-request';
import { Task } from '../entity/task';
import { TaskDetails } from '../entity/task-details';
import { MultilevelMovableService } from './multilevel-movable-service';

@Injectable({
  providedIn: 'root'
})
export class TaskService implements MultilevelMovableService<Task> {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  private getUserId(): string | null {
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

  public moveAfter(request: IdRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/move/after`, request);
  }

  public moveAsChild(request: IdRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/move/asChild`, request);
  }

  public moveAsFirst(taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/move/asFirst`, null);
  }

  public updateCollapse(taskId: number, request: BooleanRequest):  Observable<Task> {
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

  public addTaskAsChild(request: AddTaskRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.post<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/children`, request);
  }

  updateTaskProject(request: IdRequest, taskId: number): Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/project`, request);
  }

  updateTaskPriority(request: PriorityRequest, taskId: number): Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/priority`, request);
  }

  public moveAfterDaily(request: IdRequest, taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/daily/move/after`, request);
  }

  public moveAsFirstDaily(taskId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/daily/move/asFirst`, null);
  }

  public moveAsFirstWithDate(taskId: number, request: DateRequest):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/daily/move/asFirstWithDate`, request);
  }


  public duplicateTask(taskId: number):  Observable<TaskDetails[]> {
    let userId  = this.getUserId();
    return this.http.post<TaskDetails[]>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/duplicate`, null);
  }

  updateTaskLabels(request: IdCollectionRequest, taskId: number): Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/labels`, request);
  }

  public getTaskForProject(projectId: number):  Observable<TaskDetails[]> {
    let userId  = this.getUserId();
    return this.http.get<TaskDetails[]>(`${this.apiServerUrl}/${userId}/projects/${projectId}/tasks`);
  }

  archiveTask(taskId: number, request: BooleanRequest):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/archive`, request);
  }

  public getArchivedTasks(projectId: number):  Observable<TaskDetails[]> {
    let userId  = this.getUserId();
    return this.http.get<TaskDetails[]>(`${this.apiServerUrl}/${userId}/projects/${projectId}/tasks/archived`);
  }

  public rescheduleOverdueTasks(request: DateRequest):  Observable<Task[]> {
    let userId  = this.getUserId();
    return this.http.put<Task[]>(`${this.apiServerUrl}/${userId}/tasks/overdue/due`, request);
  }

  updateAssigned(request: IdRequest, taskId: number): Observable<Task> {
    let userId  = this.getUserId();
    return this.http.put<Task>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/assigned`, request);
  }
}
