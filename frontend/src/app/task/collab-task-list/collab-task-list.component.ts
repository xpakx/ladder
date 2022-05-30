import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, Input, OnInit, Renderer2, ViewChild } from '@angular/core';
import { CollabProjectData } from 'src/app/project/dto/collab-project-data';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { Task } from 'src/app/task/dto/task';
import { TaskTreeElem } from 'src/app/task/dto/task-tree-elem';
import { AddEvent } from 'src/app/common/utils/add-event';
import { DateEvent } from 'src/app/common/utils/date-event';
import { CollabTaskTreeService } from 'src/app/task/collab-task-tree.service';
import { CollabTaskService } from 'src/app/task/collab-task.service';
import { DeleteService } from 'src/app/utils/delete.service';
import { TreeService } from 'src/app/utils/tree.service';
import { MultilevelCollabTaskComponent } from 'src/app/task/multilevel-collab-task-component';

@Component({
  selector: 'app-collab-task-list',
  templateUrl: './collab-task-list.component.html',
  styleUrls: ['./collab-task-list.component.css']
})
export class CollabTaskListComponent extends MultilevelCollabTaskComponent<CollabTaskTreeService>  implements OnInit {
  @Input("collab") collab: CollabProjectData | undefined;
  @Input("project") project: ProjectTreeElem | undefined;
  @Input("initTasks") initTasks: TaskTreeElem[] = [];
  @Input("blocked") blocked: boolean = false;
    
  todayDate: Date | undefined;
  showAddTaskForm: boolean = false;
  taskData: AddEvent<TaskTreeElem> = new AddEvent<TaskTreeElem>();

  constructor(private tree: TreeService, private taskService: CollabTaskService,
    private taskTreeService: CollabTaskTreeService,
    private renderer: Renderer2, private deleteService: DeleteService) {
    super(taskTreeService, taskService);
  }

  ngOnInit(): void { }

  get tasks(): TaskTreeElem[] {
    return (this.collab && this.initTasks.length == 0) ? this.taskTreeService.getTasksByProject(this.collab.project.id) : this.initTasks;
  }

  protected getElems(): TaskTreeElem[] {
    return this.tasks;
  }

  // Task form
  openAddTaskForm(): void {
    this.closeEditTaskForm();
    this.showAddTaskForm = true;
    this.taskData = new AddEvent<TaskTreeElem>();
  }

  closeAddTaskForm(): void {
    this.showAddTaskForm = false;
  }

  openEditTaskForm(task: TaskTreeElem): void {
    this.closeAddTaskForm();
    this.taskData = new AddEvent<TaskTreeElem>(task);
  }

  closeEditTaskForm(): void {
    this.taskData = new AddEvent<TaskTreeElem>();
  }

  openEditTaskFromContextMenu(): void {
    if(this.contextTaskMenu) {
      this.closeAddTaskForm();
      this.taskData = new AddEvent<TaskTreeElem>(this.contextTaskMenu);
    }
    this.closeContextTaskMenu();
  }

  openEditTaskAbove(): void {
    if(this.contextTaskMenu) {
      this.closeAddTaskForm();
      this.taskData = new AddEvent<TaskTreeElem>(this.contextTaskMenu, false, true);
    }
    this.closeContextTaskMenu();
  }

  openEditTaskBelow(): void {
    if(this.contextTaskMenu) {
      this.closeAddTaskForm();
      this.taskData = new AddEvent<TaskTreeElem>(this.contextTaskMenu, true, false);
    }
    this.closeContextTaskMenu();
  }

  shouldEditTaskById(taskId: number): boolean {
    return this.taskObjectContains(taskId) && this.taskData.isInEditMode();
  }

  taskObjectContains(taskId: number): boolean {
   return taskId == this.taskData.object?.id;
  }

  shouldAddTaskBelowById(taskId: number): boolean {
   return this.taskObjectContains(taskId) && this.taskData.after;
  }

  shouldAddTaskAboveById(taskId: number): boolean {
   return this.taskObjectContains(taskId) && this.taskData.before;
  }

  completeTask(id: number): void {
    if(!this.collab?.taskCompletionAllowed) {return;}
    if(!this.blocked) {
      let task = this.tree.getCollabTaskById(id);
      if(task) {
      this.taskService.completeTask(id, {flag: !task.completed}).subscribe(
          (response: Task) => {
          this.taskTreeService.changeTaskCompletion(response);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
      }
    }
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
	  this.contextTaskMenu = this.getTaskById(taskId);
    this.showContextTaskMenu = true;
    this.contextTaskMenuJustOpened = true;
    this.taskContextMenuX = event.clientX-250;
    this.taskContextMenuY = event.clientY;
  }

  getTaskById(taskId: number): TaskTreeElem | undefined {
    return this.initTasks.length == 0 ? this.tree.getCollabTaskById(taskId) : this.initTasks.find((a) => a.id == taskId);
  }

  closeContextTaskMenu(): void {
    this.contextTaskMenu = undefined;
    this.showContextTaskMenu = false;
  }

  getNumOfCompletedTasksByParent(parentId: number): number {
    return this.tree.getNumOfCompletedCollabTasksByParent(parentId);
  }

  getNumOfTasksByParent(parentId: number): number {
    return this.tree.getNumOfCollabTasksByParent(parentId);
  }

  askForDelete(): void {
    if(this.contextTaskMenu) {
      this.deleteService.openModalForCollabTask(this.contextTaskMenu);
    }
    this.closeContextTaskMenu();
  }

  showSelectDateModal: boolean = false;
  dateForDateModal: Date | undefined;
  taskIdForDateModal: number | undefined;

  closeSelectDateModal(date: DateEvent): void {
    this.showSelectDateModal = false;
    if(this.taskIdForDateModal) {
      this.taskService.updateTaskDueDate({date: date.date, timeboxed: date.timeboxed}, this.taskIdForDateModal).subscribe(
          (response: Task) => {
          this.treeService.updateTaskDate(response);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
    this.dateForDateModal = undefined;
    this.taskIdForDateModal = undefined;
  }

  cancelDateSelection(): void {
    this.showSelectDateModal = false;
    this.dateForDateModal = undefined;
    this.taskIdForDateModal = undefined;
  }

  openSelectDateModal(task: TaskTreeElem): void {
    this.taskIdForDateModal = task.id;
    this.dateForDateModal = task.due ? task.due : undefined;
    this.showSelectDateModal = true;
  }

  openSelectDateModalFormContextMenu(): void {
    if(this.contextTaskMenu) {
      this.openSelectDateModal(this.contextTaskMenu);
    }
    this.closeContextTaskMenu();
  }

  showSelectPriorityModal: boolean = false;
  priorityForPriorityModal: number = 0;
  taskIdForPriorityModal: number | undefined;

  closeSelectPriorityModal(priority: number): void {
    this.showSelectPriorityModal = false;
    if(this.taskIdForPriorityModal) {
      this.taskService.updateTaskPriority({priority: priority}, this.taskIdForPriorityModal).subscribe(
          (response: Task) => {
          this.treeService.updateTaskPriority(response);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
    this.priorityForPriorityModal = 0;
    this.taskIdForPriorityModal = undefined;
  }

  cancelPrioritySelection(): void {
    this.showSelectPriorityModal = false;
    this.priorityForPriorityModal = 0;
    this.taskIdForPriorityModal = undefined;
  }

  openSelectPriorityModal(task: TaskTreeElem): void {
    this.taskIdForPriorityModal = task.id;
    this.priorityForPriorityModal = task.priority;
    this.showSelectPriorityModal = true;
  }

  openSelectPriorityModalFormContextMenu(): void {
    if(this.contextTaskMenu) {
      this.openSelectPriorityModal(this.contextTaskMenu);
    }
    this.closeContextTaskMenu();
  }

  openTask?: TaskTreeElem;

  openTaskView(task: TaskTreeElem): void {
    this.openTask = task;
  }

  closeTaskView(): void {
    this.openTask = undefined;
  }
}
