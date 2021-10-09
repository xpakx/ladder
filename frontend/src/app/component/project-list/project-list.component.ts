import { HttpErrorResponse } from '@angular/common/http';
import { AfterViewInit, Component, ElementRef, EventEmitter, OnInit, Output, Renderer2, ViewChild } from '@angular/core';
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

@Component({
  selector: 'app-project-list',
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.css']
})
export class ProjectListComponent extends MultilevelDraggableComponent<ProjectWithNameAndId, ProjectTreeElem, Project, ProjectService, ProjectTreeService>
 implements OnInit, AfterViewInit {
  contextProjectMenu: ProjectTreeElem | undefined;
  showContextProjectMenu: boolean = false;
  contextProjectMenuJustOpened: boolean = false;
  projectContextMenuX: number = 0;
  projectContextMenuY: number = 0;
  @ViewChild('projectContext', {read: ElementRef}) projectContextMenuElem!: ElementRef;

  @Output() addProject = new EventEmitter<AddEvent<ProjectTreeElem>>();

  constructor(public tree : TreeService, private projectService: ProjectService,
    protected projectTreeService: ProjectTreeService, 
    private renderer: Renderer2, private router: Router, 
    private taskService: TaskService, private deleteService: DeleteService) {
      super(projectTreeService, projectService);
     }

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
    this.addProject.emit(new AddEvent<ProjectTreeElem>());
  }

  openProjectModalWithProject() {
    this.addProject.emit(new AddEvent<ProjectTreeElem>(this.contextProjectMenu));
    this.closeContextProjectMenu();
  }

  openProjectModalAbove() {
    this.addProject.emit(new AddEvent<ProjectTreeElem>(this.contextProjectMenu, false, true));
    this.closeContextProjectMenu();
  }

  openProjectModalBelow() {
    this.addProject.emit(new AddEvent<ProjectTreeElem>(this.contextProjectMenu, true, false));
    this.closeContextProjectMenu();
  }

  // List collapsion

  switchProjectCollapse() {
    this.tree.projectCollapsed = !this.tree.projectCollapsed;
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


  askForDelete() {
    if(this.contextProjectMenu) {
      this.deleteService.openModalForProject(this.contextProjectMenu);
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
        (response: TasksWithProjects) => {
        this.tree.duplicateProject(response);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
    }

    this.closeContextProjectMenu();
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
}
