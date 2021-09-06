import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AddTaskRequest } from '../entity/add-task-request';
import { BooleanRequest } from '../entity/boolean-request';
import { FullProjectTree } from '../entity/full-project-tree';
import { IdRequest } from '../entity/id-request';
import { NameRequest } from '../entity/name-request';
import { Project } from '../entity/project';
import { ProjectDetails } from '../entity/project-details';
import { ProjectRequest } from '../entity/project-request';
import { Task } from '../entity/task';
import { UserWithData } from '../entity/user-with-data';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  public getProjectById(projectId: number):  Observable<ProjectDetails> {
    let userId  = this.getUserId();
    return this.http.get<ProjectDetails>(`${this.apiServerUrl}/${userId}/projects/${projectId}`);
  }

  public getProjectTreeById(projectId: number):  Observable<FullProjectTree> {
    let userId  = this.getUserId();
    return this.http.get<FullProjectTree>(`${this.apiServerUrl}/${userId}/projects/${projectId}/full`);
  }

  public getFullTree():  Observable<FullProjectTree[]> {
    let userId  = this.getUserId();
    return this.http.get<FullProjectTree[]>(`${this.apiServerUrl}/${userId}/projects/all`);
  }

  public getFullInfo():  Observable<UserWithData> {
    let userId  = this.getUserId();
    return this.http.get<UserWithData>(`${this.apiServerUrl}/${userId}/all`);
  }

  public addProject(request: ProjectRequest):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.post<Project>(`${this.apiServerUrl}/${userId}/projects`, request);
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

  public deleteProject(projectId: number):  Observable<any> {
    let userId  = this.getUserId();
    return this.http.delete<any>(`${this.apiServerUrl}/${userId}/projects/${projectId}`);
  }

  public duplicateProject(projectId: number):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.post<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/duplicate`, null);
  }

  public addTask(request: AddTaskRequest, projectId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.post<Task>(`${this.apiServerUrl}/${userId}/projects/${projectId}/tasks`, request);
  }

  private getUserId() {
    return localStorage.getItem("user_id");
  }
}
