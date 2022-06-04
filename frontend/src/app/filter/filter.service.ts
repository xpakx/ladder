import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { BooleanRequest } from '../common/dto/boolean-request';
import { Filter } from 'src/app/filter/dto/filter';
import { FilterRequest } from 'src/app/filter/dto/filter-request';
import { IdRequest } from '../common/dto/id-request';
import { MovableService } from 'src/app/common/movable-service';

@Injectable({
  providedIn: 'root'
})
export class FilterService  implements MovableService<Filter> {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  private getUserId() {
    return localStorage.getItem("user_id");
  }

  private getHeaders(): HttpHeaders {
    let token = localStorage.getItem("token");
    return new HttpHeaders({'Authorization':`Bearer ${token}`});
  }

  public addFilter(request: FilterRequest):  Observable<Filter> {
    let userId  = this.getUserId();
    return this.http.post<Filter>(`${this.apiServerUrl}/${userId}/filters`, request, { headers: this.getHeaders() });
  }

  public updateFilter(filterId: number, request: FilterRequest):  Observable<Filter> {
    let userId  = this.getUserId();
    return this.http.put<Filter>(`${this.apiServerUrl}/${userId}/filters/${filterId}`, request, { headers: this.getHeaders() });
  }

  public deleteFilter(filterId: number):  Observable<any> {
    let userId  = this.getUserId();
    return this.http.delete<any>(`${this.apiServerUrl}/${userId}/filters/${filterId}`, { headers: this.getHeaders() });
  }

  public updateFilterFav(filterId: number, request: BooleanRequest):  Observable<Filter> {
    let userId  = this.getUserId();
    return this.http.put<Filter>(`${this.apiServerUrl}/${userId}/filters/${filterId}/favorite`, request, { headers: this.getHeaders() });
  }

  public addFilterAfter(request: FilterRequest, filterId: number):  Observable<Filter> {
    let userId  = this.getUserId();
    return this.http.post<Filter>(`${this.apiServerUrl}/${userId}/filters/${filterId}/after`, request, { headers: this.getHeaders() });
  }

  public addFilterBefore(request: FilterRequest, filterId: number):  Observable<Filter> {
    let userId  = this.getUserId();
    return this.http.post<Filter>(`${this.apiServerUrl}/${userId}/filters/${filterId}/before`, request, { headers: this.getHeaders() });
  }

  public moveAfter(request: IdRequest, filterId: number):  Observable<Filter> {
    let userId  = this.getUserId();
    return this.http.put<Filter>(`${this.apiServerUrl}/${userId}/filters/${filterId}/move/after`, request, { headers: this.getHeaders() });
  }

  public moveAsFirst(filterId: number):  Observable<Filter> {
    let userId  = this.getUserId();
    return this.http.put<Filter>(`${this.apiServerUrl}/${userId}/filters/${filterId}/move/asFirst`, null, { headers: this.getHeaders() });
  }
}
