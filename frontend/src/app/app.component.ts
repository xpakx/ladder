import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Project } from './entity/project';
import { ProjectRequest } from './entity/project-request';
import { ProjectService } from './service/project.service';
import { TreeService } from './service/tree.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'ladder';
  displayAddProject: string = "none";
  projectFav: boolean = false;
  addProjForm: FormGroup;

  constructor(public tree : TreeService, private projectService: ProjectService, 
    private fb: FormBuilder) {
      this.addProjForm = this.fb.group({
        name: ['', Validators.required],
        color: ['', Validators.required]
      });
     }


  openProjectModal() {
    this.displayAddProject = "block";
  }

  closeProjectModal() {
    this.displayAddProject = "none";
    this.addProjForm.reset();
    this.projectFav = false;
  }

  addProjectModal() {
    let request: ProjectRequest = {
      name: this.addProjForm.controls.name.value,
      color: this.addProjForm.controls.color.value,
      parentId: null,
      favorite: this.projectFav
    };
    
    this.closeProjectModal();
    this.projectService.addProject(request).subscribe(
      (response: Project) => {
        this.tree.addNewProject(response, 0);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }

  switchProjectFav() {
    this.projectFav = !this.projectFav;
  }
}
