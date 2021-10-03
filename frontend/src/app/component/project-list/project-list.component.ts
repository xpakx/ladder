import { HttpErrorResponse } from '@angular/common/http';
import { AfterViewInit, Component, ElementRef, EventEmitter, OnInit, Output, Renderer2, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { DndDropEvent } from 'ngx-drag-drop';
import { Project } from 'src/app/entity/project';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { Task } from 'src/app/entity/task';
import { ProjectService } from 'src/app/service/project.service';
import { TaskService } from 'src/app/service/task.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-project-list',
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.css']
})
export class ProjectListComponent implements OnInit, AfterViewInit {
  contextProjectMenu: ProjectTreeElem | undefined;
  showContextProjectMenu: boolean = false;
  contextProjectMenuJustOpened: boolean = false;
  projectContextMenuX: number = 0;
  projectContextMenuY: number = 0;
  @ViewChild('projectContext', {read: ElementRef}) projectContextMenuElem!: ElementRef;

  @Output() addProjectModal = new EventEmitter<boolean>();
  @Output() addProjectModalAbove = new EventEmitter<ProjectTreeElem | undefined>();
  @Output() addProjectModalBelow = new EventEmitter<ProjectTreeElem | undefined>();
  @Output() addProjectModalEdit = new EventEmitter<ProjectTreeElem | undefined>();

  constructor(public tree : TreeService, private projectService: ProjectService, 
    private renderer: Renderer2, private router: Router, 
    private taskService: TaskService) { }

  ngOnInit(): void {
  }

  ngAfterViewInit() {
    this.renderer.listen('window', 'click',(e:Event)=>{
      if(this.showContextProjectMenu &&
        !this.projectContextMenuElem.nativeElement.contains(e.target)){
          if(this.contextProjectMenuJustOpened) {
            this.contextProjectMenuJustOpened = false
          } else {
            this.showContextProjectMenu = false;
          }
      }
    });
  }

  //Project modal window

  openProjectModal() {
    this.addProjectModal.emit(true);
  }

  openProjectModalWithProject() {
    this.addProjectModalEdit.emit(this.contextProjectMenu);
    this.closeContextProjectMenu();
  }

  openProjectModalAbove() {
    this.addProjectModalAbove.emit(this.contextProjectMenu);
    this.closeContextProjectMenu();
  }

  openProjectModalBelow() {
    this.addProjectModalBelow.emit(this.contextProjectMenu);
    this.closeContextProjectMenu();
  }

  // List collapsion

  switchProjectCollapse() {
    this.tree.projectCollapsed = !this.tree.projectCollapsed;
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
    return projects.find((a) => a.collapsed) ? true : false;
  }

  // Navigation

  toProject(id: number) {
    this.router.navigate(['/project/'+id]);
  }

  // Project context menu

  openContextProjectMenu(event: MouseEvent, projectId: number) {
	  this.contextProjectMenu = this.tree.getProjectById(projectId);
    this.showContextProjectMenu = true;
    this.contextProjectMenuJustOpened = true;
    this.projectContextMenuX = event.clientX;
    this.projectContextMenuY = event.clientY;
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
    this.closeContextProjectMenu();
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

    this.closeContextProjectMenu();
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

    this.closeContextProjectMenu();
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
    let id = Number(event.data);
    this.projectService.moveProjectToBeginning(id).subscribe(
        (response: Project) => {
        this.tree.moveProjectAsFirst(response);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }

  hideDropZone(project: ProjectTreeElem): boolean {
    return this.isDragged(project.id) || 
    this.isParentDragged(project.parentList) || 
    this.isParentCollapsed(project.parentList);
  }

  onDropTask(event: DndDropEvent, project: ProjectTreeElem) {
    let id = Number(event.data);
    this.taskService.updateTaskProject({id: project.id}, id).subscribe(
        (response: Task, proj: ProjectTreeElem = project ) => {
        this.tree.moveTaskToProject(response, proj);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }


  getListForDropzones(i: number, project: ProjectTreeElem) {
    let dropzones = project.indent - this.amountOfDropzones(i, project);
    return project.parentList.slice(-dropzones);
  }

  private amountOfDropzones(i: number, project: ProjectTreeElem): number {
    return project.hasChildren ?
      this.findFirstWithSmallerIndentAndReturnIndent(i + 1, project.indent) : this.indentForPosition(i + 1);
  }

  getAmountOfNormalDropzones(i: number, project: ProjectTreeElem): number {
    return this.amountOfDropzones(i, project);
  }


  calculateAdditionalDropzones(i: number, project: ProjectTreeElem): boolean {
    if(!project.hasChildren) {
     return project.indent > this.indentForPosition(i+1);
    } 
    if(!project.collapsed) {
      return false;
    }
    return this.findFirstWithSmallerIndentAndReturnIndent(i+1, project.indent) < project.indent;
  }

  private findFirstWithSmallerIndentAndReturnIndent(index: number, indent: number): number {
    for (let i = index; i < this.tree.getProjects().length; i++) {
      if (indent >= this.tree.getProjects()[i].indent) {
        return this.tree.getProjects()[i].indent;
      }
    }
    return 0;
  }

  private indentForPosition(i: number): number {
    if(this.outOfBound(i)) {
      return 0;
    } else {
      return this.tree.getProjects()[i].indent;
    }
    
  }

  private outOfBound(i: number): boolean {
    return i >= this.tree.getProjects().length;
  }
}
