import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, EventEmitter, OnInit, Output, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { DndDropEvent } from 'ngx-drag-drop';
import { Project } from '../dto/project';
import { ProjectTreeElem } from '../dto/project-tree-elem';
import { ProjectWithNameAndId } from '../dto/project-with-name-and-id';
import { Task } from 'src/app/task/dto/task';
import { TasksWithProjects } from 'src/app/project/dto/tasks-with-projects';
import { AddEvent } from 'src/app/common/utils/add-event';
import { DeleteService } from 'src/app/utils/delete.service';
import { ProjectTreeService } from '../project-tree.service';
import { ProjectService } from '../project.service';
import { TaskService } from 'src/app/task/task.service';
import { TreeService } from 'src/app/utils/tree.service';
import { MultilevelDraggableComponent } from '../../common/multilevel-draggable-component';
import { Animations } from '../../common/animations';
import { ContextMenuElem } from '../../context-menu/context-menu/context-menu-elem';
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
  @Output("hideScroll") hideScrollEvent = new EventEmitter<boolean>();

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

  openProjectModal(): void {
    this.addProject.emit(new AddEvent<ProjectTreeElem>());
  }

  openProjectModalWithProject(project: ProjectTreeElem): void {
    this.addProject.emit(new AddEvent<ProjectTreeElem>(project));
  }

  openProjectModalAbove(project: ProjectTreeElem): void {
    this.addProject.emit(new AddEvent<ProjectTreeElem>(project, false, true));
  }

  openProjectModalBelow(project: ProjectTreeElem): void {
    this.addProject.emit(new AddEvent<ProjectTreeElem>(project, true, false));
  }

  // List collapsion

  switchProjectCollapse(): void {
    this.tree.projectCollapsed = !this.tree.projectCollapsed;
  }

  // Navigation

  toProject(id: number): void {
    this.router.navigate(['/project/'+id]);
    this.navEvent.emit(true);
  }

  toArchive(): void {
    this.router.navigate(['/archive/']);
    this.navEvent.emit(true);
  }

  // Project context menu
  prepareContextMenu(): void {
      this.contextMenu.push(MenuElems.addProjectAbove);
      this.contextMenu.push(MenuElems.addProjectBelow);
      this.contextMenu.push(MenuElems.editProject);
      this.contextMenu.push(this.favElem);
      this.contextMenu.push(MenuElems.duplicate);
      this.contextMenu.push(MenuElems.archiveProject);
      this.contextMenu.push(MenuElems.deleteProject);
  }

  openContextProjectMenu(event: MouseEvent, projectId: number): void {
	  this.contextProjectMenu = this.tree.getProjectById(projectId);
    this.showContextProjectMenu = true;
    this.hideScrollEvent.emit(true);
    this.updateFavElem();
    this.projectContextMenuX = event.clientX;
    this.projectContextMenuY = event.clientY;
  }

  private updateFavElem(): void {
    if (this.contextProjectMenu?.favorite) {
      this.favElem.name = MenuElems.deleteFromFavs.name;
      this.favElem.icon = MenuElems.deleteFromFavs.icon;
    } else {
      this.favElem.name = MenuElems.addToFavs.name;
      this.favElem.icon = MenuElems.addToFavs.icon;
    }
  }

  closeContextMenu(code: number): void {
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

  closeContextProjectMenu(): void {
    this.contextProjectMenu = undefined;
    this.showContextProjectMenu = false;
    this.hideScrollEvent.emit(false);
  }

  askForDelete(project: ProjectTreeElem): void {
    this.deleteService.openModalForProject(project);
  }

  updateProjectFav(project: ProjectTreeElem): void {
    this.projectService.updateProjectFav(project.id, {flag: !project.favorite}).subscribe(
        (response: Project) => {
        this.tree.changeFav(response);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }

  duplicateProject(project: ProjectTreeElem): void {
    this.projectService.duplicateProject(project.id).subscribe(
        (response: TasksWithProjects, mainId: number = project.id) => {
        this.tree.duplicateProject(response, mainId);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }

  // Drag'n'drop

  onDropTask(event: DndDropEvent, project: ProjectTreeElem): void {
    let id = Number(event.data);
    this.taskService.updateTaskProject({id: project.id}, id).subscribe(
        (response: Task, proj: ProjectTreeElem = project ) => {
        this.tree.moveTaskToProject(response, proj);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }

  archiveProject(project: ProjectTreeElem): void {
    this.projectService.archiveProject(project.id, {flag:true}).subscribe(
        (response: Project) => {
        this.tree.archiveProject(response);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }
}
