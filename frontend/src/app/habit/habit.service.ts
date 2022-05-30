import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { BooleanRequest } from '../entity/boolean-request';
import { Habit } from 'src/app/habit/dto/habit';
import { HabitCompletion } from 'src/app/habit/dto/habit-completion';
import { HabitRequest } from 'src/app/habit/dto/habit-request';
import { IdRequest } from '../entity/id-request';
import { PriorityRequest } from '../entity/priority-request';
import { MovableService } from 'src/app/service/movable-service';

@Injectable({
  providedIn: 'root'
})
export class HabitService implements MovableService<Habit> {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  private getUserId() {
    return localStorage.getItem("user_id");
  }

  public deleteHabit(habitId: number):  Observable<any> {
    let userId  = this.getUserId();
    return this.http.delete<any>(`${this.apiServerUrl}/${userId}/habits/${habitId}`);
  }

  public updateHabitPriority(request: PriorityRequest, habitId: number): Observable<Habit> {
    let userId  = this.getUserId();
    return this.http.put<Habit>(`${this.apiServerUrl}/${userId}/habits/${habitId}/priority`, request);
  }

  public moveAfter(request: IdRequest, habitId: number):  Observable<Habit> {
    let userId  = this.getUserId();
    return this.http.put<Habit>(`${this.apiServerUrl}/${userId}/habits/${habitId}/move/after`, request);
  }

  public moveAsFirst(habitId: number):  Observable<Habit> {
    let userId  = this.getUserId();
    return this.http.put<Habit>(`${this.apiServerUrl}/${userId}/habits/${habitId}/move/asFirst`, null);
  }

  public addHabit(request: HabitRequest, projectId: number | undefined):  Observable<Habit> {
    let userId  = this.getUserId();
    if(projectId) {
      return this.http.post<Habit>(`${this.apiServerUrl}/${userId}/projects/${projectId}/habits`, request);
    } else {
      return this.http.post<Habit>(`${this.apiServerUrl}/${userId}/projects/inbox/habits`, request);
    }  
  }

  public updateHabit(request: HabitRequest, habitId: number): Observable<Habit> {
    let userId  = this.getUserId();
    return this.http.put<Habit>(`${this.apiServerUrl}/${userId}/habits/${habitId}`, request);
  }

  public addHabitAfter(request: HabitRequest, habitId: number):  Observable<Habit> {
    let userId  = this.getUserId();
    return this.http.post<Habit>(`${this.apiServerUrl}/${userId}/habits/${habitId}/after`, request);
  }

  public addHabitBefore(request: HabitRequest, habitId: number):  Observable<Habit> {
    let userId  = this.getUserId();
    return this.http.post<Habit>(`${this.apiServerUrl}/${userId}/habits/${habitId}/before`, request);
  }

  public updateHabitProject(request: IdRequest, habitId: number): Observable<Habit> {
    let userId  = this.getUserId();
    return this.http.put<Habit>(`${this.apiServerUrl}/${userId}/habits/${habitId}/project`, request);
  }

  public duplicateHabit(habitId: number): Observable<Habit> {
    let userId  = this.getUserId();
    return this.http.post<Habit>(`${this.apiServerUrl}/${userId}/habits/${habitId}/duplicate`, null);
  }

  public completeHabit(habitId: number, request: BooleanRequest):  Observable<HabitCompletion> {
    let userId  = this.getUserId();
    return this.http.put<HabitCompletion>(`${this.apiServerUrl}/${userId}/habits/${habitId}/complete`, request);
  }
}
