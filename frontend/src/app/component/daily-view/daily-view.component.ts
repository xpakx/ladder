import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, OnInit, Renderer2, ViewChild, ViewChildren } from '@angular/core';
import { Router } from '@angular/router';
import { DndDropEvent } from 'ngx-drag-drop';
import { LabelDetails } from 'src/app/entity/label-details';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { Task } from 'src/app/entity/task';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { DeleteService } from 'src/app/service/delete.service';
import { TaskTreeService } from 'src/app/service/task-tree.service';
import { TaskService } from 'src/app/service/task.service';
import { TreeService } from 'src/app/service/tree.service';
import { DraggableComponent } from '../abstract/draggable-component';
import { TaskDailyListComponent } from '../task-daily-list/task-daily-list.component';

@Component({
  selector: 'app-daily-view',
  templateUrl: './daily-view.component.html',
  styleUrls: ['./daily-view.component.css']
})
export class DailyViewComponent extends DraggableComponent<TaskTreeElem, Task, TaskService, TaskTreeService>
implements OnInit {
  public invalid: boolean = false;
  public message: string = '';
  todayDate: Date = new Date();
  @ViewChildren(TaskDailyListComponent) listComponents!: TaskDailyListComponent[];

  constructor(private router: Router, public tree: TreeService, 
    private taskService: TaskService, private taskTreeService: TaskTreeService,
    private renderer: Renderer2, private deleteService: DeleteService) {
    super(taskTreeService, taskService);
   }

  ngOnInit(): void {
    if(!this.tree.isLoaded()) {
      this.router.navigate(["load"]);
    }

    this.todayDate = new Date();
  }

  get tasks(): TaskTreeElem[] {
    return [...this.tree.getByDate(this.todayDate)].sort((a,b) => a.dailyOrder - b.dailyOrder);
  }

  get collabTasks(): TaskTreeElem[] {
    return [...this.tree.getCollabByDate(this.todayDate)];
  }

  get overdue(): TaskTreeElem[] {
    return this.tree.getByDateOverdue(this.todayDate);
  }

  protected getElems(): TaskTreeElem[] {
    return this.tasks;
  }

  showAddTaskForm: boolean = false;
  taskData: AddEvent<TaskTreeElem> = new AddEvent<TaskTreeElem>();

  // Task form
  openAddTaskForm(): void {
    this.closeChildrenEditTaskForms();
    this.showAddTaskForm = true;
    this.taskData = new AddEvent<TaskTreeElem>();
  }

  closeChildrenEditTaskForms(): void {
    for(let child of this.listComponents) {
      child.closeEditTaskForm();
    }
  }

  closeAddTaskForm(): void {
    this.showAddTaskForm = false;
  }

  contextTaskMenu: TaskTreeElem | undefined;
  showContextTaskMenu: boolean = false;
  contextTaskMenuJustOpened: boolean = false;
  taskContextMenuX: number = 0;
  taskContextMenuY: number = 0;
  @ViewChild('taskContext', {read: ElementRef}) taskContextMenuElem!: ElementRef;

  ngAfterViewInit(): void {
    this.renderer.listen('window', 'click',(e:Event)=>{
      if(this.showContextTaskMenu && 
        !this.taskContextMenuElem.nativeElement.contains(e.target)){
        if(this.contextTaskMenuJustOpened) {
          this.contextTaskMenuJustOpened = false
        } else {
          this.showContextTaskMenu = false;
        }
      }
    })
  }

  openContextTaskMenu(event: MouseEvent, taskId: number): void {
    this.contextTaskMenu = this.tree.getTaskById(taskId);
    this.showContextTaskMenu = true;
    this.contextTaskMenuJustOpened = true;
    this.taskContextMenuX = event.clientX-250;
    this.taskContextMenuY = event.clientY;
  }

  closeContextTaskMenu(): void {
    this.contextTaskMenu = undefined;
    this.showContextTaskMenu = false;
  }

  showSelectDateModal: boolean = false;

  closeSelectDateModal(date: Date | undefined): void {
    this.showSelectDateModal = false;
    this.taskService.rescheduleOverdueTasks({date: date, timeboxed: false}).subscribe(
      (response: Task[]) => {
        this.taskTreeService.updateTasksDate(response);
    },
    (error: HttpErrorResponse) => {
    
    });
  }

  cancelDateSelection(): void {
    this.showSelectDateModal = false;
  }

  openSelectDateModal(): void {
    this.showSelectDateModal = true;
  }

  activateDragNDrop: boolean = false;

  onDragStart(): void {
    this.activateDragNDrop = true;
  }

  onDragEnd(): void {
    this.activateDragNDrop = false;
  }
}
