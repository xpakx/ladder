import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CollaborationWithOwner } from 'src/app/collaboration/dto/collaboration-with-owner';
import { UserMin } from 'src/app/user/dto/user-min';
import { ProjectService } from 'src/app/project/project.service';

@Component({
  selector: 'app-assign-modal',
  templateUrl: './assign-modal.component.html',
  styleUrls: ['./assign-modal.component.css']
})
export class AssignModalComponent implements OnInit {
  @Input() projectId?: number;

  @Output() closeEvent = new EventEmitter<UserMin>();
  @Output() cancelEvent = new EventEmitter<boolean>();
  collaborators: CollaborationWithOwner[] = [];

  constructor(private service: ProjectService) { }

  ngOnInit(): void {
    if(this.projectId) {
      this.service.getCollaborators(this.projectId).subscribe(
        (response: CollaborationWithOwner[]) => {
          this.collaborators = response;
        },
        (error: HttpErrorResponse) => {}
      );
    }
  }

  close(): void {
    this.cancelEvent.emit(true);
  }

  assign(user: UserMin): void {
    this.closeEvent.emit(user);
  }

  assignMyself(): void {
    let id = Number(localStorage.getItem("user_id"));
    let username = localStorage.getItem('username');
    this.assign({id: id, username: username ? username : ''});
  }

}
