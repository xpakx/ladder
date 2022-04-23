import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, EventEmitter, OnInit, Output, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { DndDropEvent } from 'ngx-drag-drop';
import { Project } from 'src/app/entity/project';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { ProjectWithNameAndId } from 'src/app/entity/project-with-name-and-id';
import { Task } from 'src/app/entity/task';
import { TasksWithProjects } from 'src/app/entity/tasks-with-projects';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { DeleteService } from 'src/app/service/delete.service';
import { ProjectTreeService } from 'src/app/service/project-tree.service';
import { ProjectService } from 'src/app/service/project.service';
import { TaskService } from 'src/app/service/task.service';
import { TreeService } from 'src/app/service/tree.service';
import { MultilevelDraggableComponent } from '../abstract/multilevel-draggable-component';
import { Animations } from '../common/animations';
import { ContextMenuElem } from '../context-menu/context-menu-elem';
import { Codes, MenuElems } from './project-list-context-codes';

@Component({
  selector: 'app-project-list',
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.css'],
  animations: [Animations.collapseTrigger]
})
export class ProjectListComponent extends MultilevelDraggableComponent<ProjectWithNameAndId, ProjectTreeElem, Project, ProjectService, ProjectTreeService>
 implements OnInit {
  contextProjectMenu: ProjectTreeElem | undefined;
  showContextProjectMenu: boolean = false;
  projectContextMenuX: number = 0;
  projectContextMenuY: number = 0;
  @ViewChild('projectContext', {read: ElementRef}) projectContextMenuElem!: ElementRef;

  @Output() addProject = new EventEmitter<AddEvent<ProjectTreeElem>>();
  @Output() navEvent = new EventEmitter<boolean>();

  contextMenu: ContextMenuElem[] = [];
  favElem: ContextMenuElem = {name: MenuElems.addToFavs.name, icon: MenuElems.addToFavs.icon, code: MenuElems.addToFavs.code};

  constructor(public tree : TreeService, private projectService: ProjectService,
    protected projectTreeService: ProjectTreeService, private router: Router, 
    private taskService: TaskService, private deleteService: DeleteService) {
      super(projectTreeService, projectService);
     }

  ngOnInit(): void {
    this.prepareContextMenu();
  }

  //Project modal window

  openProjectModal() {
    this.addProject.emit(new AddEvent<ProjectTreeElem>());
  }

  openProjectModalWithProject(project: ProjectTreeElem) {
    this.addProject.emit(new AddEvent<ProjectTreeElem>(project));
  }

  openProjectModalAbove(project: ProjectTreeElem) {
    this.addProject.emit(new AddEvent<ProjectTreeElem>(project, false, true));
  }

  openProjectModalBelow(project: ProjectTreeElem) {
    this.addProject.emit(new AddEvent<ProjectTreeElem>(project, true, false));
  }

  // List collapsion

  switchProjectCollapse() {
    this.tree.projectCollapsed = !this.tree.projectCollapsed;
  }

  // Navigation

  toProject(id: number) {
    this.router.navigate(['/project/'+id]);
    this.navEvent.emit(true);
  }

  toArchive() {
    this.router.navigate(['/archive/']);
    this.navEvent.emit(true);
  }

  // Project context menu
  prepareContextMenu() {
      this.contextMenu.push(MenuElems.addProjectAbove);
      this.contextMenu.push(MenuElems.addProjectBelow);
      this.contextMenu.push(MenuElems.editProject);
      this.contextMenu.push(this.favElem);
      this.contextMenu.push(MenuElems.duplicate);
      this.contextMenu.push(MenuElems.archiveProject);
      this.contextMenu.push(MenuElems.deleteProject);
  }

  openContextProjectMenu(event: MouseEvent, projectId: number) {
	  this.contextProjectMenu = this.tree.getProjectById(projectId);
    this.showContextProjectMenu = true;
    if(this.contextProjectMenu?.favorite) {
      this.favElem.name =  MenuElems.deleteFromFavs.name;
      this.favElem.icon =  MenuElems.deleteFromFavs.icon;
    } else {
      this.favElem.name =  MenuElems.addToFavs.name;
      this.favElem.icon =  MenuElems.addToFavs.icon;
    }
    this.projectContextMenuX = event.clientX;
    this.projectContextMenuY = event.clientY;
  }

  closeContextMenu(code: number) {
    if(!this.contextProjectMenu) {return}
    let project = this.contextProjectMenu;
    this.closeContextProjectMenu();
    
    switch(code) {
      case(Codes.addProjectAbove): { this.openProjectModalAbove(project); break }
      case(Codes.addProjectBelow): { this.openProjectModalBelow(project); break }
      case(Codes.editProject): { this.openProjectModalWithProject(project); break }
      case(Codes.addToFavs): { this.updateProjectFav(project); break }
      case(Codes.duplicate): { this.duplicateProject(project); break }
      case(Codes.archiveProject): { this.archiveProject(project); break }
      case(Codes.deleteProject): { this.askForDelete(project); break }
    }
  }

  closeContextProjectMenu() {
    this.contextProjectMenu = undefined;
    this.showContextProjectMenu = false;
  }


  askForDelete(project: ProjectTreeElem) {
    this.deleteService.openModalForProject(project);
  }

  updateProjectFav(project: ProjectTreeElem) {
    this.projectService.updateProjectFav(project.id, {flag: !project.favorite}).subscribe(
        (response: Project) => {
        this.tree.changeFav(response);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }

  duplicateProject(project: ProjectTreeElem) {
    this.projectService.duplicateProject(project.id).subscribe(
        (response: TasksWithProjects, mainId: number = project.id) => {
        this.tree.duplicateProject(response, mainId);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }

  // Drag'n'drop

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

  archiveProject(project: ProjectTreeElem) {
    this.projectService.archiveProject(project.id, {flag:true}).subscribe(
        (response: Project) => {
        this.tree.archiveProject(response);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }
}
