import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, Renderer2, ViewChild, AfterViewInit, ViewChildren, QueryList } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Project } from './entity/project';
import { ProjectRequest } from './entity/project-request';
import { ProjectTreeElem } from './entity/project-tree-elem';
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
  displayAddTask: string = "none";
  projectFav: boolean = false;
  addProjForm: FormGroup;
  collapseProjects: boolean = true;
  collapseLabels: boolean = true;
  collapseFilters: boolean = true;
  hideMenu: boolean = false;
  collapsedProjectsIds: number[] = [];
  contextProjectMenu: ProjectTreeElem | undefined;
  showContextProjectMenu: boolean = false;
  projectContextMenuX: number = 0;
  projectContextMenuY: number = 0;
  @ViewChild('projectContext', {read: ElementRef}) projectContextMenuElem!: ElementRef;
  addAfter: number | undefined;
  addBefore: number | undefined;
  projectToEditId: number | undefined;

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

  openProjectModalEdit() {
    if(this.contextProjectMenu) {
      this.projectToEditId = this.contextProjectMenu.id;
      this.addProjForm.setValue({
        name: this.contextProjectMenu.name,
        color: this.contextProjectMenu.color
      });
      this.openProjectModal();
    }
  }

  closeProjectModal() {
    this.displayAddProject = "none";
    this.addProjForm.reset();
    this.projectFav = false;
    this.addAfter = undefined;
    this.addBefore = undefined;
    this.projectToEditId = undefined;
  }

  addProjectModal() {
    let request: ProjectRequest = {
      name: this.addProjForm.controls.name.value,
      color: this.addProjForm.controls.color.value,
      parentId: null,
      favorite: this.projectFav
    };

    let afterId: number | undefined = this.addAfter;
    let beforeId: number | undefined = this.addBefore;
    let editId: number | undefined = this.projectToEditId;
    this.closeProjectModal();
    if(editId) {
      this.editProjectModal(request, editId);
    } else if(afterId) {
      this.addAfterProjectModal(request, afterId);
    } else if(beforeId) {
      this.addBeforeProjectModal(request, beforeId);
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

  addBeforeProjectModal(request: ProjectRequest, id: number) {
    this.projectService.addProjectBefore(request, id).subscribe(
      (response: Project) => {
        this.tree.addNewProject(response, 0);
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

  addAfterProjectModal(request: ProjectRequest, id: number) {
    this.projectService.addProjectAfter(request, id).subscribe(
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
	  this.contextProjectMenu = this.tree.getProjectById(projectId);
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
      this.addBefore = this.contextProjectMenu.id;
      this.displayAddProject = "block";
    }
  }

  openProjectModalBelow() {
    if(this.contextProjectMenu) {
      this.addAfter = this.contextProjectMenu.id;
      this.displayAddProject = "block";
    }
  }

  deleteProject() {
    if(this.contextProjectMenu) {
      let deletedProjectId: number = this.contextProjectMenu.id;
      this.projectService.deleteProject(deletedProjectId).subscribe(
        (response: any, projectId: number = deletedProjectId) => {
        this.tree.deleteProject(projectId);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
    }
  }

  updateProjectFav() {
    if(this.contextProjectMenu) {
      this.projectService.updateProjectFav(this.contextProjectMenu.id, {flag: !this.contextProjectMenu.favorite}).subscribe(
        (response: Project) => {
        this.tree.changeFav(response);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
    }
  }

  duplicateProject() {
    if(this.contextProjectMenu) {
      this.projectService.duplicateProject(this.contextProjectMenu.id).subscribe(
        (response: Project) => {
        this.tree.addNewProject(response, 0);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
    }
  }

  openAddTaskModal() {
    this.displayAddTask = "block";
  }

  closeAddTaskModal() {
    this.displayAddTask = "none";
  }
}
