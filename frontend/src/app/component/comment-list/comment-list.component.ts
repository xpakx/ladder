import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit } from '@angular/core';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { Page } from 'src/app/entity/page';
import { CommentService } from '../../service/comment.service';
import { TaskCommentDetails } from '../../entity/task-comment-details';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TaskComment } from 'src/app/entity/task-comment';

@Component({
  selector: 'app-comment-list',
  templateUrl: './comment-list.component.html',
  styleUrls: ['./comment-list.component.css']
})
export class CommentListComponent implements OnInit {
  @Input('task') task?: TaskTreeElem;
  comments:TaskCommentDetails[] = [];
  commentForm: FormGroup | undefined;

  constructor(private commentService: CommentService, private fb: FormBuilder) {}

  ngOnInit(): void {
    this.commentForm = this.fb.group({
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

  sendComment() {
    if(this.task && this.commentForm) {
      this.commentService.addComment({content: this.commentForm.controls.content.value}, 
        this.task.id).subscribe(
        (response: TaskComment) => {
          this.comments.push(
              {
                id: response.id,
                content: response.content,
                createdAt: response.createdAt,
              }
            );
      },
      (error: HttpErrorResponse) => {
      
      });
    }
  }

  deleteComment(id: number) {
    if(this.task && this.commentForm) {
      this.commentService.deleteComment(id).subscribe(
        (response: any, commentId: number = id) => {
          this.comments = this.comments.filter((a) => a.id == commentId);
      },
      (error: HttpErrorResponse) => {
      
      });
    }
  }

}
