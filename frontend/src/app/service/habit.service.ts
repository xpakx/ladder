import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class HabitService {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  private getUserId() {
    return localStorage.getItem("user_id");
  }

  public deleteHabit(habitId: number):  Observable<any> {
    let userId  = this.getUserId();
    return this.http.delete<any>(`${this.apiServerUrl}/${userId}/habits/${habitId}`);
  }
}
