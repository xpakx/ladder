import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CollaborationDetails } from 'src/app/entity/collaboration-details';
import { CollaborationWithOwner } from 'src/app/entity/collaboration-with-owner';
import { Project } from 'src/app/entity/project';
import { UserMin } from 'src/app/entity/user-min';
import { ProjectService } from 'src/app/service/project.service';

@Component({
  selector: 'app-edit-collabs',
  templateUrl: './edit-collabs.component.html',
  styleUrls: ['./edit-collabs.component.css']
})
export class EditCollabsComponent implements OnInit {
  @Output() closeEvent = new EventEmitter<boolean>();
  @Input() projectId: number | undefined;
  collaborators: CollaborationWithOwner[] = [];
  addCollabForm: FormGroup;

  constructor(private fb: FormBuilder, private service: ProjectService) {
    this.addCollabForm = this.fb.group({
      id: ['', Validators.required]
    });
   }

  ngOnInit(): void {
    if(this.projectId) {
      this.service.getCollaborators(this.projectId).subscribe(
        (response: CollaborationWithOwner[]) => {
          this.collaborators = response;
        },
        (error: HttpErrorResponse) => {
        
      
        }
      );
    }
  }

  addCollaborator() {
    if(this.projectId) {
      this.service.addCollaborator({
        collaboratorId: this.addCollabForm.controls.id.value, 
        completionAllowed: this.complete, 
        editionAllowed: this.edit 
      }, this.projectId).subscribe(
        (response: Project) => {
          this.ngOnInit();
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }

  deleteCollaborator(collaboratorId: number) {
    if(this.projectId) {
      this.service.deleteCollaborator(collaboratorId, this.projectId).subscribe(
        (response: Project, collabId: number = collaboratorId) => {
          this.collaborators = this.collaborators.filter((a) => a.owner.id != collabId);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }
  
  close() {
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
}
