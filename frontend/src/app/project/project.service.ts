import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AddTaskRequest } from '../task/dto/add-task-request';
import { BooleanRequest } from '../common/dto/boolean-request';
import { IdRequest } from '../common/dto/id-request';
import { NameRequest } from '../common/dto/name-request';
import { Project } from 'src/app/project/dto/project';
import { ProjectData } from 'src/app/project/dto/project-data';
import { ProjectDetails } from 'src/app/project/dto/project-details';
import { ProjectRequest } from 'src/app/project/dto/project-request';
import { Task } from '../task/dto/task';
import { TasksWithProjects } from './dto/tasks-with-projects';
import { CollaborationRequest } from 'src/app/collaboration/dto/collaboration-request';
import { UserWithData } from 'src/app/sync/dto/user-with-data';
import { MultilevelMovableService } from 'src/app/common/multilevel-movable-service';
import { CollaborationWithOwner } from 'src/app/collaboration/dto/collaboration-with-owner';
import { Collaboration } from 'src/app/collaboration/dto/collaboration';

@Injectable({
  providedIn: 'root'
})
export class ProjectService implements MultilevelMovableService<Project>{
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

  public getProjectById(projectId: number):  Observable<ProjectDetails> {
    let userId  = this.getUserId();
    return this.http.get<ProjectDetails>(`${this.apiServerUrl}/${userId}/projects/${projectId}`, { headers: this.getHeaders() });
  }

  public getFullInfo():  Observable<UserWithData> {
    let userId  = this.getUserId();
    return this.http.get<UserWithData>(`${this.apiServerUrl}/${userId}/all`, { headers: this.getHeaders() });
  }

  public addProject(request: ProjectRequest):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.post<Project>(`${this.apiServerUrl}/${userId}/projects`, request, { headers: this.getHeaders() });
  }

  public addProjectAfter(request: ProjectRequest, projectId: number):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.post<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/after`, request, { headers: this.getHeaders() });
  }

  public addProjectBefore(request: ProjectRequest, projectId: number):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.post<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/before`, request, { headers: this.getHeaders() });
  }

  public updateProject(projectId: number, request: ProjectRequest):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}`, request, { headers: this.getHeaders() });
  }

  public updateProjectName(projectId: number, request: NameRequest):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/name`, request, { headers: this.getHeaders() });
  }

  public updateProjectParent(projectId: number, request: IdRequest):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/parent`, request, { headers: this.getHeaders() });
  }

  public updateProjectFav(projectId: number, request: BooleanRequest):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/favorite`, request, { headers: this.getHeaders() });
  }

  public updateCollapse(projectId: number, request: BooleanRequest):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/collapse`, request, { headers: this.getHeaders() });
  }

  public deleteProject(projectId: number):  Observable<any> {
    let userId  = this.getUserId();
    return this.http.delete<any>(`${this.apiServerUrl}/${userId}/projects/${projectId}`, { headers: this.getHeaders() });
  }

  public duplicateProject(projectId: number):  Observable<TasksWithProjects> {
    let userId  = this.getUserId();
    return this.http.post<TasksWithProjects>(`${this.apiServerUrl}/${userId}/projects/${projectId}/duplicate`, null, { headers: this.getHeaders() });
  }

  public addTask(request: AddTaskRequest, projectId: number | undefined):  Observable<Task> {
    let userId  = this.getUserId();
    request.due = request.due ? this.transformDate(request.due) : null;
    if(projectId) {
      return this.http.post<Task>(`${this.apiServerUrl}/${userId}/projects/${projectId}/tasks`, request, { headers: this.getHeaders() });
    } else {
      return this.http.post<Task>(`${this.apiServerUrl}/${userId}/projects/inbox/tasks`, request, { headers: this.getHeaders() });
    }
  }

  public addCollabTask(request: AddTaskRequest, projectId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.post<Task>(`${this.apiServerUrl}/${userId}/collab/projects/${projectId}/tasks`, request, { headers: this.getHeaders() });
  }

  public moveAfter(request: IdRequest, projectId: number):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/move/after`, request, { headers: this.getHeaders() });
  }

  public moveAsChild(request: IdRequest, projectId: number):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/move/asChild`, request, { headers: this.getHeaders() });
  }

  public moveAsFirst(projectId: number):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/move/asFirst`, null, { headers: this.getHeaders() });
  }

  public archiveProject(projectId: number, request: BooleanRequest):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/archive`, request, { headers: this.getHeaders() });
  }

  public archiveProjectCompletedTasks(projectId: number, request: BooleanRequest):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/tasks/completed/archive`, request, { headers: this.getHeaders() });
  }

  public getArchivedProjects():  Observable<ProjectDetails[]> {
    let userId  = this.getUserId();
    return this.http.get<ProjectDetails[]>(`${this.apiServerUrl}/${userId}/projects/archived`, { headers: this.getHeaders() });
  }

  public getArchivedProject(projectId: number):  Observable<ProjectData> {
    let userId  = this.getUserId();
    return this.http.get<ProjectData>(`${this.apiServerUrl}/${userId}/projects/${projectId}/data/archived`, { headers: this.getHeaders() });
  }

  public getProjectData(projectId: number):  Observable<ProjectData> {
    let userId  = this.getUserId();
    return this.http.get<ProjectData>(`${this.apiServerUrl}/${userId}/projects/${projectId}/data`, { headers: this.getHeaders() });
  }

  public addCollaborator(request: CollaborationRequest, projectId: number):  Observable<CollaborationWithOwner> {
    let userId  = this.getUserId();
    return this.http.post<CollaborationWithOwner>(`${this.apiServerUrl}/${userId}/projects/${projectId}/collaborators`, request, { headers: this.getHeaders() });
  }

  public deleteCollaborator(collaboratorId: number, projectId: number):  Observable<any> {
    let userId  = this.getUserId();
    return this.http.delete<any>(`${this.apiServerUrl}/${userId}/projects/${projectId}/collaborators/${collaboratorId}`, { headers: this.getHeaders() });
  }

  public getCollaborators(projectId: number): Observable<CollaborationWithOwner[]> {
    let userId  = this.getUserId();
    return this.http.get<CollaborationWithOwner[]>(`${this.apiServerUrl}/${userId}/projects/${projectId}/collaborators`, { headers: this.getHeaders() });
  }

  public switchEdit(request: BooleanRequest, collabId: number): Observable<Collaboration> {
    let userId  = this.getUserId();
    return this.http.put<Collaboration>(`${this.apiServerUrl}/${userId}/collab/${collabId}/edit`, request, { headers: this.getHeaders() });
  }

  public switchComplete(request: BooleanRequest, collabId: number): Observable<Collaboration> {
    let userId  = this.getUserId();
    return this.http.put<Collaboration>(`${this.apiServerUrl}/${userId}/collab/${collabId}/complete`, request, { headers: this.getHeaders() });
  }
}
