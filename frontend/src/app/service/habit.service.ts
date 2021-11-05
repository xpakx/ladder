import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Habit } from '../entity/habit';
import { PriorityRequest } from '../entity/priority-request';

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

  updateHabitPriority(request: PriorityRequest, habitId: number): Observable<Habit> {
    let userId  = this.getUserId();
    return this.http.put<Habit>(`${this.apiServerUrl}/${userId}/habits/${habitId}/priority`, request);
  }
}
