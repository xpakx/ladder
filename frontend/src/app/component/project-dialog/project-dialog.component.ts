import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Project } from 'src/app/entity/project';
import { ProjectRequest } from 'src/app/entity/project-request';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { ProjectService } from 'src/app/service/project.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-project-dialog',
  templateUrl: './project-dialog.component.html',
  styleUrls: ['./project-dialog.component.css']
})
export class ProjectDialogComponent implements OnInit {
  addProjForm: FormGroup;
  projectFav: boolean = false;

  @Output() closeEvent = new EventEmitter<boolean>();
  @Input() data: AddEvent<ProjectTreeElem> | undefined;
  project: ProjectTreeElem | undefined;
  after: boolean = false;
  before: boolean = false;
  editMode: boolean = false;

  constructor(private fb: FormBuilder, public tree : TreeService, 
    private projectService: ProjectService) { 
    this.addProjForm = this.fb.group({
      name: ['', Validators.required],
      color: ['#888', Validators.required]
    });
  }

  ngOnInit(): void {
    if(this.data) {
      this.project = this.data.object;
      this.after = this.data.after;
      this.before = this.data.before;
    }
    if(this.project && !this.after && !this.before) {
      this.editMode = true;
    }
    if(this.editMode && this.project) {
      this.addProjForm.setValue({
        name: this.project.name,
        color: this.project.color
      });
    }
  }

  switchProjectFav() {
    this.projectFav = !this.projectFav;
  }

  closeProjectModal() {
    this.closeEvent.emit(true);
  }

  addProjectModal() {
    let request: ProjectRequest = {
      name: this.addProjForm.controls.name.value,
      color: this.addProjForm.controls.color.value,
      parentId: null,
      favorite: this.projectFav
    };
    
    this.closeProjectModal();
    
    if(this.project && this.after) {
      this.addAfterProjectModal(request, this.project);
    } else if(this.project && this.before) {
      this.addBeforeProjectModal(request, this.project);
    } else if(this.project) {
      this.editProjectModal(request, this.project.id);
    } else {
      this.addEndProjectModal(request)
    }
  }

  addEndProjectModal(request: ProjectRequest) {
    this.projectService.addProject(request).subscribe(
      (response: Project) => {
        this.tree.addNewProject(response, 0);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }

  addBeforeProjectModal(request: ProjectRequest, projectBefore: ProjectTreeElem) {
    this.projectService.addProjectBefore(request, projectBefore.id).subscribe(
      (response: Project, beforeId: number = projectBefore.id, indent: number = projectBefore.indent) => {
        this.tree.addNewProjectBefore(response, indent, beforeId);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }

  editProjectModal(request: ProjectRequest, id: number) {
    this.projectService.updateProject(id, request).subscribe(
      (response: Project) => {
        this.tree.updateProject(response, id);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }

  addAfterProjectModal(request: ProjectRequest, projectAfter: ProjectTreeElem) {
    this.projectService.addProjectAfter(request, projectAfter.id).subscribe(
      (response: Project, afterId: number = projectAfter.id, indent: number = projectAfter.indent) => {
        this.tree.addNewProjectAfter(response, indent, afterId);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }

}
