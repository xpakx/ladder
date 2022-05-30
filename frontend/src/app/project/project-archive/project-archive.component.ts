import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, OnInit, Renderer2, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HabitDetails } from 'src/app/habit/dto/habit-details';
import { ProjectData } from 'src/app/project/dto/project-data';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { TaskTreeElem } from 'src/app/task/dto/task-tree-elem';
import { ProjectService } from 'src/app/project/project.service';
import { RedirectionService } from 'src/app/utils/redirection.service';
import { TaskService } from 'src/app/task/task.service';
import { TreeService } from 'src/app/utils/tree.service';

@Component({
  selector: 'app-project-archive',
  templateUrl: './project-archive.component.html',
  styleUrls: ['./project-archive.component.css']
})
export class ProjectArchiveComponent implements OnInit {
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
      this.redirService.setAddress("archive/project/"+this.route.snapshot.params.id)
      this.router.navigate(["load"]);
    }

    this.route.params.subscribe(routeParams => {
      this.loadProject(routeParams.id);
    });    
  }

  loadProject(id: number) {
    this.id = id;
    this.project = {
      id: id, 
      name: '', 
      parent: null, 
      color: '', 
      order: 0, 
      realOrder: 0, 
      hasChildren: false, 
      indent: 0, 
      parentList: [], 
      favorite: false, 
      collapsed: false, 
      modifiedAt: new Date(),
      collaborative: false    
    }
    this.loadArchivedTasks(id);
  }

  tasks: TaskTreeElem[] = [];
  habits: HabitDetails[] = [];

  loadArchivedTasks(id: number) {
      this.projectService.getArchivedProject(id).subscribe(
        (response: ProjectData) => {
          this.tasks = this.tree.transformTasks(response.tasks);
          this.project = this.tree.transformProject(response.project);
          this.habits = response.habits;
        },
        (error: HttpErrorResponse) => {
        
        }
      );
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
}
