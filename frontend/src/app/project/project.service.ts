import { HttpClient } from '@angular/common/http';
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
import { MultilevelMovableService } from 'src/app/service/multilevel-movable-service';
import { CollaborationWithOwner } from 'src/app/collaboration/dto/collaboration-with-owner';
import { Collaboration } from 'src/app/collaboration/dto/collaboration';

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

  public addCollabTask(request: AddTaskRequest, projectId: number):  Observable<Task> {
    let userId  = this.getUserId();
    return this.http.post<Task>(`${this.apiServerUrl}/${userId}/collab/projects/${projectId}/tasks`, request);
  }

  private getUserId(): string | null {
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

  public archiveProjectCompletedTasks(projectId: number, request: BooleanRequest):  Observable<Project> {
    let userId  = this.getUserId();
    return this.http.put<Project>(`${this.apiServerUrl}/${userId}/projects/${projectId}/tasks/completed/archive`, request);
  }

  public getArchivedProjects():  Observable<ProjectDetails[]> {
    let userId  = this.getUserId();
    return this.http.get<ProjectDetails[]>(`${this.apiServerUrl}/${userId}/projects/archived`);
  }

  public getArchivedProject(projectId: number):  Observable<ProjectData> {
    let userId  = this.getUserId();
    return this.http.get<ProjectData>(`${this.apiServerUrl}/${userId}/projects/${projectId}/data/archived`);
  }

  public getProjectData(projectId: number):  Observable<ProjectData> {
    let userId  = this.getUserId();
    return this.http.get<ProjectData>(`${this.apiServerUrl}/${userId}/projects/${projectId}/data`);
  }

  public addCollaborator(request: CollaborationRequest, projectId: number):  Observable<CollaborationWithOwner> {
    let userId  = this.getUserId();
    return this.http.post<CollaborationWithOwner>(`${this.apiServerUrl}/${userId}/projects/${projectId}/collaborators`, request);
  }

  public deleteCollaborator(collaboratorId: number, projectId: number):  Observable<any> {
    let userId  = this.getUserId();
    return this.http.delete<any>(`${this.apiServerUrl}/${userId}/projects/${projectId}/collaborators/${collaboratorId}`);
  }

  public getCollaborators(projectId: number): Observable<CollaborationWithOwner[]> {
    let userId  = this.getUserId();
    return this.http.get<CollaborationWithOwner[]>(`${this.apiServerUrl}/${userId}/projects/${projectId}/collaborators`);
  }

  public switchEdit(request: BooleanRequest, collabId: number): Observable<Collaboration> {
    let userId  = this.getUserId();
    return this.http.put<Collaboration>(`${this.apiServerUrl}/${userId}/collab/${collabId}/edit`, request);
  }

  public switchComplete(request: BooleanRequest, collabId: number): Observable<Collaboration> {
    let userId  = this.getUserId();
    return this.http.put<Collaboration>(`${this.apiServerUrl}/${userId}/collab/${collabId}/complete`, request);
  }
}
