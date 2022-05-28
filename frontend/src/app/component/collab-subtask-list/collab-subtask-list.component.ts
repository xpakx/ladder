import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, Input, OnInit, Renderer2, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { LabelDetails } from 'src/app/entity/label-details';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { Task } from 'src/app/entity/task';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { DateEvent } from 'src/app/entity/utils/date-event';
import { CollabTaskTreeService } from 'src/app/service/collab-task-tree.service';
import { CollabTaskService } from 'src/app/service/collab-task.service';
import { DeleteService } from 'src/app/service/delete.service';
import { TreeService } from 'src/app/service/tree.service';
import { MultilevelCollabTaskComponent } from '../abstract/multilevel-collab-task-component';

@Component({
  selector: 'app-collab-subtask-list',
  templateUrl: './collab-subtask-list.component.html',
  styleUrls: ['./collab-subtask-list.component.css']
})
export class CollabSubtaskListComponent extends MultilevelCollabTaskComponent<CollabTaskTreeService> 
implements OnInit {
  public invalid: boolean = false;
  public message: string = '';
  todayDate: Date = new Date();
  @Input("task") parent?: TaskTreeElem;
  @Input("taskList") taskList: TaskTreeElem[] = [];

  constructor(private router: Router, public tree: TreeService, 
    protected taskService: CollabTaskService, protected treeService: CollabTaskTreeService,
    private renderer: Renderer2, private deleteService: DeleteService) {
    super(treeService, taskService);
  }
  
  ngOnInit(): void {
    if(!this.tree.isLoaded()) {
      this.router.navigate(["load"]);
    }

    this.todayDate = new Date();
    if(this.parent && this.parent.project) {
      this.project = this.tree.getCollabProjectById(this.parent.project.id);
    }
  }

  get tasks(): TaskTreeElem[] {
    if(this.taskList.length > 0) {
      return this.taskList;
    }
    return this.parent ? [...this.treeService.getByParentId(this.parent.id)] : [];
  }

  get list(): boolean {
    return this.taskList.length > 0;
  }

  get newTaskData(): AddEvent<TaskTreeElem> | undefined {
    return new AddEvent<TaskTreeElem>(this.parent, false, false, true);
  }

  protected getElems(): TaskTreeElem[] {
    return this.tasks;
  }

  showAddTaskForm: boolean = false;
  taskData: AddEvent<TaskTreeElem> = new AddEvent<TaskTreeElem>();

  getProjectColor(id: number): string {
    let project = this.tree.getCollabProjectById(id)
    return project ? project.color : ""
  }

  toProject(): void {
    if(this.contextTaskMenu && this.contextTaskMenu.project) {
      this.router.navigate(['/collab/'+this.contextTaskMenu.project.id]);
    } 
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

  shouldEditTaskById(taskId: number): boolean {
    return this.taskObjectContains(taskId) && this.taskData.isInEditMode();
  }

  taskObjectContains(taskId: number): boolean {
    return taskId == this.taskData.object?.id;
  }

  completeTask(id: number): void {
    let task = this.treeService.getById(id);
    if(task) {
    this.taskService.completeTask(id, {flag: !task.completed}).subscribe(
        (response: Task) => {
        this.treeService.changeTaskCompletion(response);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
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
    this.contextTaskMenu = this.tree.getCollabTaskById(taskId);
    this.showContextTaskMenu = true;
    this.contextTaskMenuJustOpened = true;
    this.taskContextMenuX = event.clientX-250;
    this.taskContextMenuY = event.clientY;
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
          this.tree.updateCollabTaskDate(response);
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
          this.tree.updateCollabTaskPriority(response);
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
}
