import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { BooleanRequest } from '../common/dto/boolean-request';
import { Habit } from 'src/app/habit/dto/habit';
import { HabitCompletion } from 'src/app/habit/dto/habit-completion';
import { HabitRequest } from 'src/app/habit/dto/habit-request';
import { IdRequest } from '../common/dto/id-request';
import { PriorityRequest } from '../common/dto/priority-request';
import { MovableService } from 'src/app/common/movable-service';

@Injectable({
  providedIn: 'root'
})
export class HabitService implements MovableService<Habit> {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  private getUserId() {
    return localStorage.getItem("user_id");
  }

  private getHeaders(): HttpHeaders {
    let token = localStorage.getItem("token");
    return new HttpHeaders({'Authorization':`Bearer ${token}`});
  }

  public deleteHabit(habitId: number):  Observable<any> {
    let userId  = this.getUserId();
    return this.http.delete<any>(`${this.apiServerUrl}/${userId}/habits/${habitId}`, { headers: this.getHeaders() });
  }

  public updateHabitPriority(request: PriorityRequest, habitId: number): Observable<Habit> {
    let userId  = this.getUserId();
    return this.http.put<Habit>(`${this.apiServerUrl}/${userId}/habits/${habitId}/priority`, request, { headers: this.getHeaders() });
  }

  public moveAfter(request: IdRequest, habitId: number):  Observable<Habit> {
    let userId  = this.getUserId();
    return this.http.put<Habit>(`${this.apiServerUrl}/${userId}/habits/${habitId}/move/after`, request, { headers: this.getHeaders() });
  }

  public moveAsFirst(habitId: number):  Observable<Habit> {
    let userId  = this.getUserId();
    return this.http.put<Habit>(`${this.apiServerUrl}/${userId}/habits/${habitId}/move/asFirst`, null, { headers: this.getHeaders() });
  }

  public addHabit(request: HabitRequest, projectId: number | undefined):  Observable<Habit> {
    let userId  = this.getUserId();
    if(projectId) {
      return this.http.post<Habit>(`${this.apiServerUrl}/${userId}/projects/${projectId}/habits`, request, { headers: this.getHeaders() });
    } else {
      return this.http.post<Habit>(`${this.apiServerUrl}/${userId}/projects/inbox/habits`, request, { headers: this.getHeaders() });
    }  
  }

  public updateHabit(request: HabitRequest, habitId: number): Observable<Habit> {
    let userId  = this.getUserId();
    return this.http.put<Habit>(`${this.apiServerUrl}/${userId}/habits/${habitId}`, request, { headers: this.getHeaders() });
  }

  public addHabitAfter(request: HabitRequest, habitId: number):  Observable<Habit> {
    let userId  = this.getUserId();
    return this.http.post<Habit>(`${this.apiServerUrl}/${userId}/habits/${habitId}/after`, request, { headers: this.getHeaders() });
  }

  public addHabitBefore(request: HabitRequest, habitId: number):  Observable<Habit> {
    let userId  = this.getUserId();
    return this.http.post<Habit>(`${this.apiServerUrl}/${userId}/habits/${habitId}/before`, request, { headers: this.getHeaders() });
  }

  public updateHabitProject(request: IdRequest, habitId: number): Observable<Habit> {
    let userId  = this.getUserId();
    return this.http.put<Habit>(`${this.apiServerUrl}/${userId}/habits/${habitId}/project`, request, { headers: this.getHeaders() });
  }

  public duplicateHabit(habitId: number): Observable<Habit> {
    let userId  = this.getUserId();
    return this.http.post<Habit>(`${this.apiServerUrl}/${userId}/habits/${habitId}/duplicate`, null, { headers: this.getHeaders() });
  }

  public completeHabit(habitId: number, request: BooleanRequest):  Observable<HabitCompletion> {
    let userId  = this.getUserId();
    return this.http.put<HabitCompletion>(`${this.apiServerUrl}/${userId}/habits/${habitId}/complete`, request, { headers: this.getHeaders() });
  }
}
