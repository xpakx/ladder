import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TaskCommentDetails } from 'src/app/comment/dto/task-comment-details';
import { TaskComment } from 'src/app/comment/dto/task-comment';
import { Page } from '../entity/page';
import { AddCommentRequest } from '../comment/dto/add-comment-request';

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private apiServerUrl = environment.apiServerUrl;

  constructor(private http: HttpClient) { }

  private getUserId() {
    return localStorage.getItem("user_id");
  }

  public getCommentsForTask(taskId: number):  Observable<Page<TaskCommentDetails>> {
    let userId  = this.getUserId();
    return this.http.get<Page<TaskCommentDetails>>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/comments`);
  }

  public addComment(request: AddCommentRequest, taskId: number):  Observable<TaskComment> {
    let userId  = this.getUserId();
    return this.http.post<TaskComment>(`${this.apiServerUrl}/${userId}/tasks/${taskId}/comments`, request);
  }

  public deleteComment(commentId: number): Observable<any> {
    let userId  = this.getUserId();
    return this.http.delete<any>(`${this.apiServerUrl}/${userId}/tasks/comments/${commentId}`);
  }
}