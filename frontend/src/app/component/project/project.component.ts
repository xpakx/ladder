import { HttpErrorResponse } from '@angular/common/http';
import { AfterViewInit, Component, ElementRef, OnInit, Renderer2, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { LabelDetails } from 'src/app/entity/label-details';
import { Project } from 'src/app/entity/project';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { Task } from 'src/app/entity/task';
import { TaskDetails } from 'src/app/entity/task-details';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { DeleteService } from 'src/app/service/delete.service';
import { ProjectService } from 'src/app/service/project.service';
import { RedirectionService } from 'src/app/service/redirection.service';
import { TaskTreeService } from 'src/app/service/task-tree.service';
import { TaskService } from 'src/app/service/task.service';
import { TreeService } from 'src/app/service/tree.service';
import { MultilevelTaskComponent } from '../abstract/multilevel-task-component';

@Component({
  selector: 'app-project',
  templateUrl: './project.component.html',
  styleUrls: ['./project.component.css']
})
export class ProjectComponent implements OnInit {
  public invalid: boolean = false;
  public message: string = '';
  project: ProjectTreeElem | undefined;
  id!: number;

  public view: number = 0;

  constructor(private router: Router, private route: ActivatedRoute, 
    private tree: TreeService,  private redirService: RedirectionService, 
    private renderer: Renderer2, private projectService: ProjectService,
    private taskService: TaskService) {  }

  ngOnInit(): void {
    if(!this.tree.isLoaded()) {
      this.redirService.setAddress("project/"+this.route.snapshot.params.id)
      this.router.navigate(["load"]);
    }

    this.route.params.subscribe(routeParams => {
      this.loadProject(routeParams.id);
    });    
  }

  loadProject(id: number) {
    this.id = id;
    this.project = this.tree.getProjectById(id);
  }

  chooseTab(num: number) {
    this.view = num;
  }

  showContextMenu: boolean = false;
  contextMenuJustOpened: boolean = false;
  contextMenuX: number = 0;
  contextMenuY: number = 0;
  @ViewChild('taskContext', {read: ElementRef}) taskContextMenuElem!: ElementRef;


  ngAfterViewInit() {
    this.renderer.listen('window', 'click',(e:Event)=>{
      if(this.showContextMenu && 
        !this.taskContextMenuElem.nativeElement.contains(e.target)){
        if(this.contextMenuJustOpened) {
          this.contextMenuJustOpened = false
        } else {
          this.showContextMenu = false;
        }
      }
    })
  }

  openContextMenu(event: MouseEvent) {
    this.showContextMenu = true;
    this.contextMenuJustOpened = true;
    this.contextMenuX = event.clientX-250;
    this.contextMenuY = event.clientY;
  }

  closeContextTaskMenu() {
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
}