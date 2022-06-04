import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { BooleanRequest } from '../common/dto/boolean-request';
import { IdRequest } from '../common/dto/id-request';
import { Label } from 'src/app/label/dto/label';
import { LabelRequest } from 'src/app/label/dto/label-request';
import { MovableService } from 'src/app/common/movable-service';

@Injectable({
  providedIn: 'root'
})
export class LabelService implements MovableService<Label> {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  private getUserId() {
    return localStorage.getItem("user_id");
  }

  private getHeaders(): HttpHeaders {
    let token = localStorage.getItem("token");
    return new HttpHeaders({'Authorization':`Bearer ${token}`});
  }

  public addLabel(request: LabelRequest):  Observable<Label> {
    let userId  = this.getUserId();
    return this.http.post<Label>(`${this.apiServerUrl}/${userId}/labels`, request, { headers: this.getHeaders() });
  }

  public updateLabel(labelId: number, request: LabelRequest):  Observable<Label> {
    let userId  = this.getUserId();
    return this.http.put<Label>(`${this.apiServerUrl}/${userId}/labels/${labelId}`, request, { headers: this.getHeaders() });
  }

  public deleteLabel(labelId: number):  Observable<any> {
    let userId  = this.getUserId();
    return this.http.delete<any>(`${this.apiServerUrl}/${userId}/labels/${labelId}`, { headers: this.getHeaders() });
  }

  public updateLabelFav(labelId: number, request: BooleanRequest):  Observable<Label> {
    let userId  = this.getUserId();
    return this.http.put<Label>(`${this.apiServerUrl}/${userId}/labels/${labelId}/favorite`, request, { headers: this.getHeaders() });
  }

  public addLabelAfter(request: LabelRequest, labelId: number):  Observable<Label> {
    let userId  = this.getUserId();
    return this.http.post<Label>(`${this.apiServerUrl}/${userId}/labels/${labelId}/after`, request, { headers: this.getHeaders() });
  }

  public addLabelBefore(request: LabelRequest, labelId: number):  Observable<Label> {
    let userId  = this.getUserId();
    return this.http.post<Label>(`${this.apiServerUrl}/${userId}/labels/${labelId}/before`, request, { headers: this.getHeaders() });
  }

  public moveAfter(request: IdRequest, labelId: number):  Observable<Label> {
    let userId  = this.getUserId();
    return this.http.put<Label>(`${this.apiServerUrl}/${userId}/labels/${labelId}/move/after`, request, { headers: this.getHeaders() });
  }

  public moveAsFirst(labelId: number):  Observable<Label> {
    let userId  = this.getUserId();
    return this.http.put<Label>(`${this.apiServerUrl}/${userId}/labels/${labelId}/move/asFirst`, null, { headers: this.getHeaders() });
  }
}
