import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, Renderer2, ViewChild, AfterViewInit, ViewChildren, QueryList } from '@angular/core';
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
export class AppComponent implements AfterViewInit {
  title = 'ladder';
  displayAddProject: string = "none";
  projectFav: boolean = false;
  addProjForm: FormGroup;
  collapseProjects: boolean = true;
  collapseLabels: boolean = true;
  collapseFilters: boolean = true;
  hideMenu: boolean = false;
  collapsedProjectsIds: number[] = [];
  contextProjectMenu: number| undefined;
  showContextProjectMenu: boolean = false;
  projectContextMenuX: number = 0;
  projectContextMenuY: number = 0;
  @ViewChild('projectContext', {read: ElementRef}) projectContextMenuElem!: ElementRef;

  constructor(public tree : TreeService, private projectService: ProjectService, 
    private fb: FormBuilder, private router: Router, private renderer: Renderer2) {
    this.addProjForm = this.fb.group({
      name: ['', Validators.required],
      color: ['', Validators.required]
    });
  }

  ngAfterViewInit() {
    this.renderer.listen('window', 'click',(e:Event)=>{
      if(this.showContextProjectMenu &&
        e.target !== this.projectContextMenuElem.nativeElement){
          this.showContextProjectMenu = false;
      }
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

  openContextProjectMenu(event: MouseEvent, projectId: number) {
	  this.contextProjectMenu = projectId;
    this.showContextProjectMenu = true;
    this.projectContextMenuX = event.clientX;
    this.projectContextMenuY = event.clientY;
    event.stopPropagation();
  }

  closeContextProjectMenu() {
    this.contextProjectMenu = undefined;
    this.showContextProjectMenu = false;
  }

  openProjectModalAbove() {
    if(this.contextProjectMenu) {
      this.displayAddProject = "block";
    }
  }

  openProjectModalBelow() {
    if(this.contextProjectMenu) {
      this.displayAddProject = "block";
    }
  }

  deleteProject() {
    if(this.contextProjectMenu) {
      let deletedProjectId: number = this.contextProjectMenu;
      this.projectService.deleteProject(this.contextProjectMenu).subscribe(
        (response: any, projectId: number = deletedProjectId) => {
        this.tree.deleteProject(projectId);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
    }
  }
  
}
