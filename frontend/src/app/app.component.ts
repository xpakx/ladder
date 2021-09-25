import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, Renderer2, ViewChild, AfterViewInit } from '@angular/core';
import { Router } from '@angular/router';
import { DndDropEvent } from 'ngx-drag-drop';
import { Project } from './entity/project';
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
  projectFav: boolean = false;
  collapseLabels: boolean = true;
  collapseFilters: boolean = true;
  hideMenu: boolean = false;
  contextProjectMenu: ProjectTreeElem | undefined;
  showContextProjectMenu: boolean = false;
  projectContextMenuX: number = 0;
  projectContextMenuY: number = 0;
  @ViewChild('projectContext', {read: ElementRef}) projectContextMenuElem!: ElementRef;

  addAfter: boolean = false;
  addBefore: boolean = false;
  projectForModalWindow: ProjectTreeElem | undefined;
  displayProjectModal: boolean = false;

  displayAddTask: boolean = false;

  constructor(public tree : TreeService, private projectService: ProjectService, 
    private router: Router, private renderer: Renderer2) {
  }

  ngAfterViewInit() {
    this.renderer.listen('window', 'click',(e:Event)=>{
      if(this.showContextProjectMenu &&
        e.target !== this.projectContextMenuElem.nativeElement){
          this.showContextProjectMenu = false;
      }
    });
  }

  //Project modal window

  openProjectModal() {
    this.displayProjectModal = true;
  }

  openProjectModalWithProject() {
    this.projectForModalWindow = this.contextProjectMenu;
    this.openProjectModal();
  }

  openProjectModalAbove() {
    this.addBefore = true;
    this.openProjectModalWithProject();
  }

  openProjectModalBelow() {
    this.addAfter = true;
    this.openProjectModalWithProject();
  }

  closeProjectModal() {
    this.displayProjectModal = false;
    this.projectForModalWindow = undefined;
    this.addAfter = false;
    this.addBefore = false;
  }

  // List collapsion

  switchProjectCollapse() {
    this.tree.projectCollapsed = !this.tree.projectCollapsed;
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
    let project = this.tree.getProjectById(projectId);
    if(project) {
      project.collapsed = !project.collapsed;
      this.projectService.updateProjectCollapse(project.id, {flag: project.collapsed}).subscribe(
        (response: Project) => {
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }
  
  isProjectCollapsed(projectId: number): boolean {
    let project = this.tree.getProjectById(projectId);
    if(project) {
      return project.collapsed ? true : false;
    }
	  return false;
  }
  
  isParentCollapsed(projects: ProjectTreeElem[]): boolean {
	  for(let project of projects) {
      if(project.collapsed) {
        return true;
      }
	  }
	  return false;
  }

  // Navigation

  toHome() {
    this.router.navigate(['/']);
  }

  toProject(id: number) {
    this.router.navigate(['/project/'+id]);
  }

  toInbox() {
    this.router.navigate(['/inbox']);
  }

  toUpcoming() {
    this.router.navigate(['/upcoming']);
  }

  // Project context menu

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

  // Task modal window

  openAddTaskModal() {
    this.displayAddTask = true;
  }

  closeAddTaskModal() {
    this.displayAddTask = false;
  }

  // Drag'n'drop

  draggedId: number | undefined;

  onDragStart(id: number) {
	  this.draggedId = id;
  }

  onDragEnd() {
	  this.draggedId = undefined;
  }

  isDragged(id: number): boolean {
    return this.draggedId == id;
  }

  isParentDragged(projects: ProjectTreeElem[]): boolean {
	  for(let project of projects) {
      if(project.id == this.draggedId) {
        return true;
      }
	  }
	  return false;
  }
  
  onDrop(event: DndDropEvent, target: ProjectTreeElem, asChild: boolean = false) {
    let id = Number(event.data);
    if(!asChild)
    {
      this.projectService.moveProjectAfter({id: target.id}, id).subscribe(
          (response: Project, indent: number = target.indent, afterId: number = target.id) => {
          this.tree.moveProjectAfter(response, indent, afterId);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    } else {
      this.projectService.moveProjectAsChild({id: target.id}, id).subscribe(
          (response: Project, indent: number = target.indent+1, afterId: number = target.id) => {
          this.tree.moveProjectAsChild(response, indent, afterId);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }

  onDropFirst(event: DndDropEvent) {
    alert(event.data + " on first item");
  }

  hideDropZone(project: ProjectTreeElem): boolean {
    return this.isDragged(project.id) || 
    this.isParentDragged(project.parentList) || 
    this.isParentCollapsed(project.parentList);
  }
}
