import { HttpErrorResponse } from '@angular/common/http';
import { Component, DoCheck, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Project } from '../dto//project';
import { ProjectTreeElem } from '../dto/project-tree-elem';
import { TaskDetails } from 'src/app/task/dto/task-details';
import { TaskTreeElem } from 'src/app/task/dto/task-tree-elem';
import { ExportService } from 'src/app/settings/export.service';
import { ProjectService } from '../project.service';
import { RedirectionService } from 'src/app/utils/redirection.service';
import { TaskService } from 'src/app/task/task.service';
import { TreeService } from 'src/app/utils/tree.service';
import { ContextMenuElem } from '../../context-menu/context-menu/context-menu-elem';
import { TaskListComponent } from 'src/app/task/task-list/task-list.component';
import { Codes, MenuElems } from './project-context-codes';

@Component({
  selector: 'app-project',
  templateUrl: './project.component.html',
  styleUrls: ['./project.component.css']
})
export class ProjectComponent implements OnInit, DoCheck {
  public invalid: boolean = false;
  public message: string = '';
  project: ProjectTreeElem | undefined;
  id!: number;
  contextMenu: ContextMenuElem[] = [];

  public view: number = 0;

  constructor(private router: Router, private route: ActivatedRoute, 
    private tree: TreeService,  private redirService: RedirectionService, 
    private projectService: ProjectService, private taskService: TaskService, 
    private exportService: ExportService) {  }

  ngOnInit(): void {
    if(!this.tree.isLoaded()) {
      this.redirService.setAddress("project/"+this.route.snapshot.params.id)
      this.router.navigate(["load"]);
    }

    this.route.params.subscribe(routeParams => {
      this.loadProject(routeParams.id);
    });   
    this.prepareContextMenu(); 
  }

  ngDoCheck(): void {
    if(this.id) {
      if(!this.tree.getProjectById(this.id)) {
        this.router.navigate(["/"]);
      }
    }
  }

  loadProject(id: number) {
    this.id = id;
    this.project = this.tree.getProjectById(id);
    this.archivedTasks = [];
  }

  chooseTab(num: number) {
    this.view = num;
  }

  showContextMenu: boolean = false;
  contextMenuX: number = 0;
  contextMenuY: number = 0;
 
  prepareContextMenu(): void {
    this.contextMenu.push(MenuElems.archiveCompleted);
    this.contextMenu.push(MenuElems.loadArchived);
    this.contextMenu.push(MenuElems.exportToCsv);
    this.contextMenu.push(MenuElems.exportToTxt);
    this.contextMenu.push(MenuElems.showCollaborations);
  }

  openContextMenu(event: MouseEvent): void {
    this.showContextMenu = true;
    this.contextMenuX = event.clientX;
    this.contextMenuY = event.clientY;
  }

  closeContextMenu(code: number): void {
    this.closeContextActionMenu();
    
    switch(code) {
      case(Codes.archiveCompleted): { this.archiveCompleted(); break }
      case(Codes.loadArchived): { this.loadArchivedTasks(); break }
      case(Codes.exportToCsv): { this.exportToCSV(); break }
      case(Codes.exportToTxt): { this.exportToTXT(); break }
      case(Codes.showCollaborations): { this.openCollabModal(); break }
    }
  }

  closeContextActionMenu() {
    this.showContextMenu = false;
  }

  archiveCompleted() {
    if(this.project) {
      this.projectService.archiveProjectCompletedTasks(this.project.id, {flag: true}).subscribe(
        (response: Project) => {
          this.tree.deleteCompletedTasks(response.id);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }

  archivedTasks: TaskTreeElem[] = [];

  loadArchivedTasks() {
    if(this.project) {
      this.taskService.getArchivedTasks(this.project.id).subscribe(
        (response: TaskDetails[]) => {
          this.archivedTasks = this.tree.transformTasks(response);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }

  exportToCSV() {
    if(this.project) {
      this.exportService.getProjectTasksAsCSV(this.project.id).subscribe(
        (response: Blob) => {
          var csv = new Blob([response], { type: "text/csv" });
          var url= window.URL.createObjectURL(csv);
          window.open(url);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }

  exportToTXT() {
    if(this.project) {
      this.exportService.getProjectTasksAsTXT(this.project.id).subscribe(
        (response: Blob) => {
          var txt = new Blob([response], { type: "text/txt" });
          var url= window.URL.createObjectURL(txt);
          window.open(url);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }

  displayCollabModal: boolean = false;

  openCollabModal() {
    this.displayCollabModal = true;
  }

  closeCollabModal() {
    this.displayCollabModal = false;
  }


  @ViewChild("taskList") tasks?: TaskListComponent;

  updateContextMenu() {
    this.tasks?.prepareContextMenu();
  }
}
