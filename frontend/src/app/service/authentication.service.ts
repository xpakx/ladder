import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private apiServerUrl = "";

  constructor(private http: HttpClient) { }

  public authenticate(request: object):  Observable<any> {
    return this.http.post<any>(`${this.apiServerUrl}/authenticate`, request);
  }
}
