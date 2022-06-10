import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit } from '@angular/core';
import { TaskTreeElem } from 'src/app/task/dto/task-tree-elem';
import { Page } from 'src/app/common/dto/page';
import { CommentService } from '../comment.service';
import { TaskCommentDetails } from '../dto/task-comment-details';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { TaskComment } from 'src/app/comment/dto/task-comment';

export interface CommentForm {
  content: FormControl<string>;
}

@Component({
  selector: 'app-comment-list',
  templateUrl: './comment-list.component.html',
  styleUrls: ['./comment-list.component.css']
})
export class CommentListComponent implements OnInit {
  @Input('task') task?: TaskTreeElem;
  comments:TaskCommentDetails[] = [];
  commentForm: FormGroup<CommentForm> | undefined;

  constructor(private commentService: CommentService, private fb: FormBuilder) {}

  ngOnInit(): void {
    this.commentForm = this.fb.nonNullable.group({
      content: ['', Validators.required]
    });

    if(this.task) {
      this.commentService.getCommentsForTask(this.task.id).subscribe(
        (response: Page<TaskCommentDetails>) => {
          this.comments = response.content;
      },
      (error: HttpErrorResponse) => {
      
      });
    }
  }

  sendComment(): void {
    if(this.task && this.commentForm) {
      this.commentService.addComment({content: this.commentForm.controls.content.value}, 
        this.task.id).subscribe(
        (response: TaskComment) => {
          let username = localStorage.getItem('username');
          let id = Number(localStorage.getItem('user_id'));
          this.comments.push(
              {
                id: response.id,
                content: response.content,
                createdAt: response.createdAt,
                owner: {id: id, username: username ? username : ''}
              }
            );
      },
      (error: HttpErrorResponse) => {
      
      });
      this.commentForm.reset();
    }
  }

  deleteComment(id: number): void {
    if(this.task && this.commentForm) {
      this.commentService.deleteComment(id).subscribe(
        (response: any, commentId: number = id) => {
          this.comments = this.comments.filter((a) => a.id != commentId);
      },
      (error: HttpErrorResponse) => {
      
      });
    }
  }

}
