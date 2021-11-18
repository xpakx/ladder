import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { BooleanRequest } from '../entity/boolean-request';
import { Filter } from '../entity/filter';
import { FilterRequest } from '../entity/filter-request';

@Injectable({
  providedIn: 'root'
})
export class FilterService  {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  private getUserId() {
    return localStorage.getItem("user_id");
  }

  public addFilter(request: FilterRequest):  Observable<Filter> {
    let userId  = this.getUserId();
    return this.http.post<Filter>(`${this.apiServerUrl}/${userId}/filters`, request);
  }

  public updateFilter(filterId: number, request: FilterRequest):  Observable<Filter> {
    let userId  = this.getUserId();
    return this.http.put<Filter>(`${this.apiServerUrl}/${userId}/filters/${filterId}`, request);
  }

  public deleteFilter(filterId: number):  Observable<any> {
    let userId  = this.getUserId();
    return this.http.delete<any>(`${this.apiServerUrl}/${userId}/filters/${filterId}`);
  }

  public updateFilterFav(filterId: number, request: BooleanRequest):  Observable<Filter> {
    let userId  = this.getUserId();
    return this.http.put<Filter>(`${this.apiServerUrl}/${userId}/filters/${filterId}/favorite`, request);
  }
}
