import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { BooleanRequest } from '../common/dto/boolean-request';
import { CollabToken } from './dto/collab-token';
import { Collaboration } from 'src/app/collaboration/dto/collaboration';
import { CollaborationDetails } from 'src/app/collaboration/dto/collaboration-details';
import { User } from '../user/dto/user';

@Injectable({
  providedIn: 'root'
})
export class CollaborationService {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  private getUserId() {
    return localStorage.getItem("user_id");
  }
  
  public changeAcceptation(collabId: number, request: BooleanRequest):  Observable<Collaboration> {
    let userId  = this.getUserId();
    return this.http.put<Collaboration>(`${this.apiServerUrl}/${userId}/collab/${collabId}/invitation`, request);
  }

  public getInvitations():  Observable<CollaborationDetails[]> {
    let userId  = this.getUserId();
    return this.http.get<CollaborationDetails[]>(`${this.apiServerUrl}/${userId}/collab/invitations`);
  }

  public unsubscribe(projectId: number, request: BooleanRequest):  Observable<Collaboration[]> {
    let userId  = this.getUserId();
    return this.http.put<Collaboration[]>(`${this.apiServerUrl}/${userId}/collab/projects/${projectId}/subscription`, request);
  }

  public getNewToken():  Observable<User> {
    let userId  = this.getUserId();
    return this.http.put<User>(`${this.apiServerUrl}/${userId}/collab/token`, null);
  }

  public getToken():  Observable<CollabToken> {
    let userId  = this.getUserId();
    return this.http.get<CollabToken>(`${this.apiServerUrl}/${userId}/collab/token`);
  }
}
