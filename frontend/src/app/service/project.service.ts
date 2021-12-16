import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AddTaskRequest } from '../entity/add-task-request';
import { BooleanRequest } from '../entity/boolean-request';
import { IdRequest } from '../entity/id-request';
import { NameRequest } from '../entity/name-request';
import { Project } from '../entity/project';
import { ProjectDetails } from '../entity/project-details';
import { ProjectRequest } from '../entity/project-request';
import { Task } from '../entity/task';
import { TasksWithProjects } from '../entity/tasks-with-projects';
import { UserWithData } from '../entity/user-with-data';
import { MultilevelMovableService } from './multilevel-movable-service';

@Injectable({
  providedIn: 'root'
})
export class ProjectService implements MultilevelMovableService<Project>{
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  public getProjectById(projectId: number):  Observable<ProjectDetails> {
    let userId  = this.getUserId();
    return this.http.get<ProjectDetails>(`${this.apiServerUrl}/${userId}/projects/${projectId}`);
  }

  public getFullInfo():  Observable<UserWithData> {
    let userId  = this.getUserId();
    return this.http.get<UserWithData>(`${this.apiServerUrl}/${userId}/all`);
  }

  public addProject(request: ProjectRequest):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.post<Project>(`${this.apiServerUrl}/${userId}/projects`, request);
  }

  public addProjectAfter(request: ProjectRequest, projectId: number):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.post<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/after`, request);
  }

  public addProjectBefore(request: ProjectRequest, projectId: number):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.post<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/before`, request);
  }

  public updateProject(projectId: number, request: ProjectRequest):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}`, request);
  }

  public updateProjectName(projectId: number, request: NameRequest):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/name`, request);
  }

  public updateProjectParent(projectId: number, request: IdRequest):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/parent`, request);
  }

  public updateProjectFav(projectId: number, request: BooleanRequest):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/favorite`, request);
  }

  public updateCollapse(projectId: number, request: BooleanRequest):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/collapse`, request);
  }

  public deleteProject(projectId: number):  Observable<any> {
    let userId  = this.getUserId();
    return this.http.delete<any>(`${this.apiServerUrl}/${userId}/projects/${projectId}`);
  }

  public duplicateProject(projectId: number):  Observable<TasksWithProjects> {
    let userId  = this.getUserId();
    return this.http.post<TasksWithProjects>(`${this.apiServerUrl}/${userId}/projects/${projectId}/duplicate`, null);
  }

  public addTask(request: AddTaskRequest, projectId: number | undefined):  Observable<Task> {
    let userId  = this.getUserId();
    if(projectId) {
      return this.http.post<Task>(`${this.apiServerUrl}/${userId}/projects/${projectId}/tasks`, request);
    } else {
      return this.http.post<Task>(`${this.apiServerUrl}/${userId}/projects/inbox/tasks`, request);
    }
  }

  private getUserId() {
    return localStorage.getItem("user_id");
  }

  public moveAfter(request: IdRequest, projectId: number):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/move/after`, request);
  }

  public moveAsChild(request: IdRequest, projectId: number):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/move/asChild`, request);
  }

  public moveAsFirst(projectId: number):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/move/asFirst`, null);
  }

  public archiveProject(projectId: number, request: BooleanRequest):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/archive`, request);
  }

  public getArchiveprojects():  Observable<ProjectDetails[]> {
    let userId  = this.getUserId();
    return this.http.get<ProjectDetails[]>(`${this.apiServerUrl}/${userId}/projects/archived`);
  }
}
