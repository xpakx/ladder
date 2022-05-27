import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Collaboration } from 'src/app/entity/collaboration';
import { CollaborationWithOwner } from 'src/app/entity/collaboration-with-owner';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { ProjectTreeService } from 'src/app/service/project-tree.service';
import { ProjectService } from 'src/app/service/project.service';
import { TaskTreeService } from 'src/app/service/task-tree.service';

@Component({
  selector: 'app-edit-collabs',
  templateUrl: './edit-collabs.component.html',
  styleUrls: ['./edit-collabs.component.css']
})
export class EditCollabsComponent implements OnInit {
  @Output() closeEvent = new EventEmitter<boolean>();
  @Output() collabChange = new EventEmitter<boolean>();
  @Input() projectId: number | undefined;
  project: ProjectTreeElem | undefined;
  collaborators: CollaborationWithOwner[] = [];
  addCollabForm: FormGroup;

  constructor(private fb: FormBuilder, private service: ProjectService, 
    private projectTree: ProjectTreeService,
    private taskTree: TaskTreeService) {
    this.addCollabForm = this.fb.group({
      id: ['', Validators.required]
    });
   }

  ngOnInit(): void {
    if(this.projectId) {
      if(this.projectId) {
        this.project = this.projectTree.getById(this.projectId);
      }
      this.service.getCollaborators(this.projectId).subscribe(
        (response: CollaborationWithOwner[]) => {
          this.collaborators = response;
        },
        (error: HttpErrorResponse) => {
        
      
        }
      );
    }
  }

  addCollaborator(): void {
    if(this.projectId) {
      this.service.addCollaborator({
        collaborationToken: this.addCollabForm.controls.id.value, 
        completionAllowed: this.complete, 
        editionAllowed: this.edit 
      }, this.projectId).subscribe(
        (response: CollaborationWithOwner) => {
          this.collaborators.push(response);
          if(this.project) {
            this.project.collaborative = true;
          }
          this.collabChange.emit(true);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }

  deleteCollaborator(collaboratorId: number): void {
    if(this.projectId) {
      this.service.deleteCollaborator(collaboratorId, this.projectId).subscribe(
        (response: any, collabId: number = collaboratorId) => {
          let userId = this.collaborators.find((a) => a.owner.id == collabId)?.id;
          this.collaborators = this.collaborators.filter((a) => a.owner.id != collabId);
          if(this.project && userId) {
            this.taskTree.deleteAssignations(this.project.id, userId);
          }
          if(this.collaborators.length == 0 && this.project) {
            this.project.collaborative = false;
          }
          this.collabChange.emit(true);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }
  
  close(): void {
    this.closeEvent.emit(true);
  }

  edit: boolean = false;
  complete: boolean = false;

  switchEdit(): void {
    this.edit = !this.edit;
  }

  switchComplete(): void {
    this.complete = !this.complete;
  }

  changeEdit(collab: Collaboration): void {
    this.service.switchEdit({flag: !collab.editionAllowed}, collab.id).subscribe(
      (response: Collaboration) => {
        let collabToUpdate = this.collaborators.find((a) => a.id == response.id);
        if(collabToUpdate) {
          collabToUpdate.editionAllowed = response.editionAllowed;
        }
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }

  changeComplete(collab: Collaboration): void {
    this.service.switchComplete({flag: !collab.taskCompletionAllowed}, collab.id).subscribe(
      (response: Collaboration) => {
        let collabToUpdate = this.collaborators.find((a) => a.id == response.id);
        if(collabToUpdate) {
          collabToUpdate.taskCompletionAllowed = response.taskCompletionAllowed;
        }
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }
}
