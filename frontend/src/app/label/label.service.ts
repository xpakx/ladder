import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { BooleanRequest } from '../common/dto/boolean-request';
import { IdRequest } from '../common/dto/id-request';
import { Label } from 'src/app/label/dto/label';
import { LabelRequest } from 'src/app/label/dto/label-request';
import { MovableService } from 'src/app/service/movable-service';

@Injectable({
  providedIn: 'root'
})
export class LabelService implements MovableService<Label> {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  private getUserId() {
    return localStorage.getItem("user_id");
  }

  public addLabel(request: LabelRequest):  Observable<Label> {
    let userId  = this.getUserId();
    return this.http.post<Label>(`${this.apiServerUrl}/${userId}/labels`, request);
  }

  public updateLabel(labelId: number, request: LabelRequest):  Observable<Label> {
    let userId  = this.getUserId();
    return this.http.put<Label>(`${this.apiServerUrl}/${userId}/labels/${labelId}`, request);
  }

  public deleteLabel(labelId: number):  Observable<any> {
    let userId  = this.getUserId();
    return this.http.delete<any>(`${this.apiServerUrl}/${userId}/labels/${labelId}`);
  }

  public updateLabelFav(labelId: number, request: BooleanRequest):  Observable<Label> {
    let userId  = this.getUserId();
    return this.http.put<Label>(`${this.apiServerUrl}/${userId}/labels/${labelId}/favorite`, request);
  }

  public addLabelAfter(request: LabelRequest, labelId: number):  Observable<Label> {
    let userId  = this.getUserId();
    return this.http.post<Label>(`${this.apiServerUrl}/${userId}/labels/${labelId}/after`, request);
  }

  public addLabelBefore(request: LabelRequest, labelId: number):  Observable<Label> {
    let userId  = this.getUserId();
    return this.http.post<Label>(`${this.apiServerUrl}/${userId}/labels/${labelId}/before`, request);
  }

  public moveAfter(request: IdRequest, labelId: number):  Observable<Label> {
    let userId  = this.getUserId();
    return this.http.put<Label>(`${this.apiServerUrl}/${userId}/labels/${labelId}/move/after`, request);
  }

  public moveAsFirst(labelId: number):  Observable<Label> {
    let userId  = this.getUserId();
    return this.http.put<Label>(`${this.apiServerUrl}/${userId}/labels/${labelId}/move/asFirst`, null);
  }
}
