import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
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
  collapseProjects: boolean = true;
  collapseLabels: boolean = true;
  collapseFilters: boolean = true;
  hideMenu: boolean = false;
  collapsedProjectsIds: number[] = [];

  constructor(public tree : TreeService, private projectService: ProjectService, 
    private fb: FormBuilder, private router: Router) {
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

  switchProjectCollapse() {
    this.collapseProjects = !this.collapseProjects;
  }

  switchLabelCollapse() {
    this.collapseLabels = !this.collapseLabels;
  }

  switchFilterCollapse() {
    this.collapseFilters = !this.collapseFilters;
  }

  switchHideMenu() {
    this.hideMenu = !this.hideMenu;
  }
  
  collapseProject(projectId: number) {
	  const index = this.collapsedProjectsIds.indexOf(projectId);
	  if (index > -1) {
			this.collapsedProjectsIds.splice(index, 1);
	  } else {
		  this.collapsedProjectsIds.push(projectId);
	  }
  }
  
  isProjectCollapsed(projectId: number): boolean {
	  return this.collapsedProjectsIds.indexOf(projectId) > -1;
  }
  
  isParentCollapsed(projectsIds: number[]): boolean {
	  for(let id of projectsIds) {
		if(this.collapsedProjectsIds.indexOf(id) > -1) {
			return true;
		}
	  }
	  return false;
  }

  toHome() {
    this.router.navigate(['/']);
  }

  toProject(id: number) {
    this.router.navigate(['/project/'+id]);
  }
  
}
