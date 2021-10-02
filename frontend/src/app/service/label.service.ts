import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { BooleanRequest } from '../entity/boolean-request';
import { Label } from '../entity/label';
import { LabelRequest } from '../entity/label-request';

@Injectable({
  providedIn: 'root'
})
export class LabelService {
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
}
