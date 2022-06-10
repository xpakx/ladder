import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, HostListener, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Project } from '../dto/project';
import { ProjectRequest } from '../dto/project-request';
import { ProjectTreeElem } from '../dto/project-tree-elem';
import { AddEvent } from 'src/app/common/utils/add-event';
import { ProjectService } from '../project.service';
import { TreeService } from 'src/app/utils/tree.service';

export interface ProjectForm {
  name: FormControl<string>;
  color: FormControl<string>;
}

@Component({
  selector: 'app-project-dialog',
  templateUrl: './project-dialog.component.html',
  styleUrls: ['./project-dialog.component.css']
})
export class ProjectDialogComponent implements OnInit {
  form: FormGroup<ProjectForm>;
  favorite: boolean = false;

  @Output() closeEvent = new EventEmitter<boolean>();
  @Input() data: AddEvent<ProjectTreeElem> | undefined;
  elem: ProjectTreeElem | undefined;
  after: boolean = false;
  before: boolean = false;
  editMode: boolean = false;

  colors: string[] = ['#ADA', '#EDA', '#DDD', '#888', '#B8255F', '#25B87D',
  '#B83325', '#B825A9', '#FF9933', '#3399FF', '#FFFF33', '#FF3333', '#7ECC49',
  '#49CC56', '#BFCC49', '#9849CC', '#158FAD', '#AD3315', '#1543AD', '#15AD80'];
  showColors = false;

  toggleShowColors() {
    this.showColors = !this.showColors;
  }

  get color(): string {
    return this.form.controls.color.value;
  }

  constructor(private fb: FormBuilder, public tree : TreeService, 
    private projectService: ProjectService) { 
    this.form = this.fb.nonNullable.group({
      name: ['', Validators.required],
      color: ['#888', Validators.required]
    });
  }

  ngOnInit(): void {
    if(this.data) {
      this.elem = this.data.object;
      this.after = this.data.after;
      this.before = this.data.before;
    }
    if(this.elem && !this.after && !this.before) {
      this.editMode = true;
    }
    if(this.editMode && this.elem) {
      this.form.setValue({
        name: this.elem.name,
        color: this.elem.color
      });
    }
  }

  chooseColor(color: string) {
    this.form.setValue({
      name: this.form.controls.name.value,
      color: color
    });
  }

  switchFav() {
    this.favorite = !this.favorite;
  }

  closeModal() {
    this.closeEvent.emit(true);
  }

  addProjectModal() {
    let request: ProjectRequest = {
      name: this.form.controls.name.value,
      color: this.form.controls.color.value,
      parentId: null,
      favorite: this.favorite
    };
    
    this.closeModal();
    
    if(this.elem && this.after) {
      this.addAfterProjectModal(request, this.elem);
    } else if(this.elem && this.before) {
      this.addBeforeProjectModal(request, this.elem);
    } else if(this.elem) {
      this.editProjectModal(request, this.elem.id);
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

  @HostListener("window:keydown.escape", ["$event"])
  handleKeyboardEscapeEvent() {
    this.closeModal();
  }

  @HostListener("window:keydown.enter", ["$event"])
  handleKeyboardEnterEvent() {
    if(this.form?.valid) {
      this.addProjectModal();
    }
  }
}
