import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { BooleanRequest } from '../entity/boolean-request';
import { Collaboration } from '../entity/collaboration';
import { CollaborationDetails } from '../entity/collaboration-details';
import { CollaborationRequest } from '../entity/collaboration-request';

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
}
