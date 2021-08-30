import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AuthenticationRequest } from '../entity/authentication-request';
import { RegistrationRequest } from '../entity/registration-request';
import { TokenResponse } from '../entity/token-response';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  public authenticate(request: AuthenticationRequest):  Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.apiServerUrl}/authenticate`, request);
  }

  public register(request: RegistrationRequest):  Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.apiServerUrl}/register`, request);
  }
}
