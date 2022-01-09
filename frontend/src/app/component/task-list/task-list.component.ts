import { HttpErrorResponse } from '@angular/common/http';
import { AfterViewInit, Component, ElementRef, Input, OnInit, Renderer2, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { LabelDetails } from 'src/app/entity/label-details';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { Task } from 'src/app/entity/task';
import { TaskDetails } from 'src/app/entity/task-details';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { DeleteService } from 'src/app/service/delete.service';
import { RedirectionService } from 'src/app/service/redirection.service';
import { TaskTreeService } from 'src/app/service/task-tree.service';
import { TaskService } from 'src/app/service/task.service';
import { TreeService } from 'src/app/service/tree.service';
import { MultilevelTaskComponent } from '../abstract/multilevel-task-component';

@Component({
  selector: 'app-task-list',
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.css']
})
export class TaskListComponent extends MultilevelTaskComponent<TaskTreeService> 
implements OnInit, AfterViewInit {
  @Input("project") project: ProjectTreeElem | undefined;
  @Input("initTasks") initTasks: TaskTreeElem[] = [];
  @Input("blocked") blocked: boolean = false;
  
  todayDate: Date | undefined;
  showAddTaskForm: boolean = false;
  taskData: AddEvent<TaskTreeElem> = new AddEvent<TaskTreeElem>();

  constructor(private router: Router, private route: ActivatedRoute, 
    private tree: TreeService, private taskService: TaskService,
    private taskTreeService: TaskTreeService,
    private renderer: Renderer2, private deleteService: DeleteService,
    private redirService: RedirectionService) {
    super(taskTreeService, taskService);
  }

  ngOnInit(): void {
  }

  get tasks(): TaskTreeElem[] {
    return (this.project && this.initTasks.length == 0) ? this.tree.getTasksByProject(this.project.id) : this.initTasks;
  }

  protected getElems(): TaskTreeElem[] {
    return this.tasks;
  }

  // Task form
  openAddTaskForm() {
    this.closeEditTaskForm();
    this.showAddTaskForm = true;
    this.taskData = new AddEvent<TaskTreeElem>();
  }

  closeAddTaskForm() {
    this.showAddTaskForm = false;
  }

  openEditTaskForm(task: TaskTreeElem) {
    this.closeAddTaskForm();
    this.taskData = new AddEvent<TaskTreeElem>(task);
  }

  closeEditTaskForm() {
    this.taskData = new AddEvent<TaskTreeElem>();
  }

  openEditTaskFromContextMenu() {
    if(this.contextTaskMenu) {
      this.closeAddTaskForm();
      this.taskData = new AddEvent<TaskTreeElem>(this.contextTaskMenu);
    }
    this.closeContextTaskMenu();
  }

  openEditTaskAbove() {
    if(this.contextTaskMenu) {
      this.closeAddTaskForm();
      this.taskData = new AddEvent<TaskTreeElem>(this.contextTaskMenu, false, true);
    }
    this.closeContextTaskMenu();
  }

  openEditTaskBelow() {
    if(this.contextTaskMenu) {
      this.closeAddTaskForm();
      this.taskData = new AddEvent<TaskTreeElem>(this.contextTaskMenu, true, false);
    }
    this.closeContextTaskMenu();
  }

  shouldEditTaskById(taskId: number): boolean {
    return this.taskObjectContains(taskId) && this.taskData.isInEditMode();
  }

  taskObjectContains(taskId: number) {
   return taskId == this.taskData.object?.id;
  }

  shouldAddTaskBelowById(taskId: number): boolean {
   return this.taskObjectContains(taskId) && this.taskData.after;
  }

  shouldAddTaskAboveById(taskId: number): boolean {
   return this.taskObjectContains(taskId) && this.taskData.before;
  }

  completeTask(id: number) {
    if(!this.blocked) {
      let task = this.tree.getTaskById(id);
      if(task) {
      this.taskService.completeTask(id, {flag: !task.completed}).subscribe(
          (response: Task) => {
          this.tree.changeTaskCompletion(response);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
      }
    }
  }

  dateWithinWeek(date: Date): boolean {
    let dateToCompare: Date = new Date();
    dateToCompare.setDate(dateToCompare.getDate() + 9);
    dateToCompare.setHours(0);
    dateToCompare.setMinutes(0);
    dateToCompare.setSeconds(0);
    dateToCompare.setMilliseconds(0);
    return date < dateToCompare && !this.isOverdue(date);
  }

  isOverdue(date: Date): boolean {
    let dateToCompare: Date = new Date();
    dateToCompare.setHours(0);
    dateToCompare.setMinutes(0);
    dateToCompare.setSeconds(0);
    return date < dateToCompare;
  }

  sameDay(date1: Date, date2: Date): boolean {
    return date1.getFullYear() == date2.getFullYear() && date1.getDate() == date2.getDate() && date1.getMonth() == date2.getMonth();
  }

  isToday(date: Date): boolean {
    let today = new Date();
    return this.sameDay(today, date);
  }

  isTomorrow(date: Date): boolean {
    let tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return this.sameDay(tomorrow, date);
  }

  thisYear(date: Date): boolean {
    let today = new Date();
    return today.getFullYear() == date.getFullYear();
  }

  contextTaskMenu: TaskTreeElem | undefined;
  showContextTaskMenu: boolean = false;
  contextTaskMenuJustOpened: boolean = false;
  taskContextMenuX: number = 0;
  taskContextMenuY: number = 0;
  @ViewChild('taskContext', {read: ElementRef}) taskContextMenuElem!: ElementRef;


  ngAfterViewInit() {
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

  openContextTaskMenu(event: MouseEvent, taskId: number) {
	  this.contextTaskMenu = this.getTaskById(taskId);
    this.showContextTaskMenu = true;
    this.contextTaskMenuJustOpened = true;
    this.taskContextMenuX = event.clientX-250;
    this.taskContextMenuY = event.clientY;
  }

  getTaskById(taskId: number): TaskTreeElem | undefined {
    return this.initTasks.length == 0 ? this.tree.getTaskById(taskId) : this.initTasks.find((a) => a.id == taskId);
  }

  closeContextTaskMenu() {
    this.contextTaskMenu = undefined;
    this.showContextTaskMenu = false;
  }

  getNumOfCompletedTasksByParent(parentId: number): number {
    return this.tree.getNumOfCompletedTasksByParent(parentId);
  }

  getNumOfTasksByParent(parentId: number): number {
    return this.tree.getNumOfTasksByParent(parentId);
  }

  askForDelete() {
    if(this.contextTaskMenu) {
      this.deleteService.openModalForTask(this.contextTaskMenu);
    }
    this.closeContextTaskMenu();
  }

  showSelectDateModal: boolean = false;
  dateForDateModal: Date | undefined;
  taskIdForDateModal: number | undefined;

  closeSelectDateModal(date: Date | undefined) {
    this.showSelectDateModal = false;
    if(this.taskIdForDateModal) {
      this.taskService.updateTaskDueDate({date: date}, this.taskIdForDateModal).subscribe(
          (response: Task) => {
          this.tree.updateTaskDate(response);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
    this.dateForDateModal = undefined;
    this.taskIdForDateModal = undefined;
  }

  cancelDateSelection() {
    this.showSelectDateModal = false;
    this.dateForDateModal = undefined;
    this.taskIdForDateModal = undefined;
  }

  openSelectDateModal(task: TaskTreeElem) {
    this.taskIdForDateModal = task.id;
    this.dateForDateModal = task.due ? task.due : undefined;
    this.showSelectDateModal = true;
  }

  openSelectDateModalFormContextMenu() {
    if(this.contextTaskMenu) {
      this.openSelectDateModal(this.contextTaskMenu);
    }
    this.closeContextTaskMenu();
  }

  showSelectProjectModal: boolean = false;
  projectForProjectModal: ProjectTreeElem | undefined;
  taskIdForProjectModal: number | undefined;

  closeSelectProjectModal(project: ProjectTreeElem | undefined) {
    this.showSelectProjectModal = false;
    if(this.taskIdForProjectModal) {
      this.taskService.updateTaskProject({id: project? project.id : undefined}, this.taskIdForProjectModal).subscribe(
          (response: Task, proj: ProjectTreeElem | undefined = project) => {
          this.tree.moveTaskToProject(response, proj);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
    this.projectForProjectModal = undefined;
    this.taskIdForProjectModal = undefined;
  }

  cancelProjectSelection() {
    this.showSelectProjectModal = false;
    this.projectForProjectModal = undefined;
    this.taskIdForProjectModal = undefined;
  }

  openSelectProjectModal(task: TaskTreeElem) {
    this.taskIdForProjectModal = task.id;
    this.projectForProjectModal = this.project;
    this.showSelectProjectModal = true;
  }

  openSelectProjectModalFormContextMenu() {
    if(this.contextTaskMenu) {
      this.openSelectProjectModal(this.contextTaskMenu);
    }
    this.closeContextTaskMenu();
  }

  showSelectPriorityModal: boolean = false;
  priorityForPriorityModal: number = 0;
  taskIdForPriorityModal: number | undefined;

  closeSelectPriorityModal(priority: number) {
    this.showSelectPriorityModal = false;
    if(this.taskIdForPriorityModal) {
      this.taskService.updateTaskPriority({priority: priority}, this.taskIdForPriorityModal).subscribe(
          (response: Task) => {
          this.tree.updateTaskPriority(response);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
    this.priorityForPriorityModal = 0;
    this.taskIdForPriorityModal = undefined;
  }

  cancelPrioritySelection() {
    this.showSelectPriorityModal = false;
    this.priorityForPriorityModal = 0;
    this.taskIdForPriorityModal = undefined;
  }

  openSelectPriorityModal(task: TaskTreeElem) {
    this.taskIdForPriorityModal = task.id;
    this.priorityForPriorityModal = task.priority;
    this.showSelectPriorityModal = true;
  }

  openSelectPriorityModalFormContextMenu() {
    if(this.contextTaskMenu) {
      this.openSelectPriorityModal(this.contextTaskMenu);
    }
    this.closeContextTaskMenu();
  }

  getTaskLabels(task: TaskTreeElem): LabelDetails[] {
    let labels: LabelDetails[] = [];
    for(let label of task.labels) {
      let labelFromTree = this.tree.getLabelById(label.id);
      if(labelFromTree) {
        labels.push(labelFromTree);
      }
    }
    return labels;
  }

  duplicate() {
    if(this.contextTaskMenu) {
      this.taskService.duplicateTask(this.contextTaskMenu.id).subscribe(
        (response: TaskDetails[]) => {
        this.tree.duplicateTask(response);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
    }

    this.closeContextTaskMenu();
  }

  openTask?: TaskTreeElem;

  openTaskView(task: TaskTreeElem) {
    this.openTask = task;
  }

  closeTaskView() {
    this.openTask = undefined;
  }

  archiveTask() {
    if(this.contextTaskMenu) {
      this.taskService.archiveTask(this.contextTaskMenu.id, {flag:true}).subscribe(
        (response: Task) => {
        this.tree.archiveTask(response);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
    }

    this.closeContextTaskMenu();
  }

  restoreTask() {
    if(this.contextTaskMenu) {
      this.taskService.archiveTask(this.contextTaskMenu.id, {flag:false}).subscribe(
        (response: Task, tasks: TaskTreeElem[] = this.initTasks) => {
        this.tree.restoreTask(response ,tasks);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
    }

    this.closeContextTaskMenu();
  }
}
