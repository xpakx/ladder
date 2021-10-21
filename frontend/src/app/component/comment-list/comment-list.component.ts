import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit } from '@angular/core';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { Page } from 'src/app/entity/page';
import { CommentService } from '../../service/comment.service';
import { TaskCommentDetails } from '../../entity/task-comment-details';

@Component({
  selector: 'app-comment-list',
  templateUrl: './comment-list.component.html',
  styleUrls: ['./comment-list.component.css']
})
export class CommentListComponent implements OnInit {
  @Input('task') task?: TaskTreeElem;
  comments:TaskCommentDetails[] = [];

  constructor(private commentService: CommentService) {}

  ngOnInit(): void {
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
   
  }

}
