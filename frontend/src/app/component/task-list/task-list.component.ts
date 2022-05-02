import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit } from '@angular/core';
import { LabelDetails } from 'src/app/entity/label-details';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { Task } from 'src/app/entity/task';
import { TaskDetails } from 'src/app/entity/task-details';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { UserMin } from 'src/app/entity/user-min';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { DeleteService } from 'src/app/service/delete.service';
import { TaskTreeService } from 'src/app/service/task-tree.service';
import { TaskService } from 'src/app/service/task.service';
import { TreeService } from 'src/app/service/tree.service';
import { MultilevelTaskComponent } from '../abstract/multilevel-task-component';
import { ContextMenuElem } from '../context-menu/context-menu-elem';
import { Codes, MenuElems } from './task-list-context-codes';

@Component({
  selector: 'app-task-list',
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.css']
})
export class TaskListComponent extends MultilevelTaskComponent<TaskTreeService> 
implements OnInit {
  @Input("project") project: ProjectTreeElem | undefined;
  @Input("initTasks") initTasks: TaskTreeElem[] = [];
  @Input("blocked") blocked: boolean = false;
  @Input("inbox") inbox: boolean = false;
  
  todayDate: Date | undefined;
  showAddTaskForm: boolean = false;
  taskData: AddEvent<TaskTreeElem> = new AddEvent<TaskTreeElem>();
  contextMenu: ContextMenuElem[] = [];

  constructor(private tree: TreeService, private taskService: TaskService,
    private taskTreeService: TaskTreeService, private deleteService: DeleteService) {
    super(taskTreeService, taskService);
  }

  ngOnInit(): void {
    this.prepareContextMenu();
  }

  get tasks(): TaskTreeElem[] {
    if(this.project && this.initTasks.length == 0) {
      return  this.tree.getTasksByProject(this.project.id);
    }
    if(this.inbox) {
      return this.taskTreeService.getTasksFromInbox();
    }  
    return this.initTasks;
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

  openEditTaskAbove(task: TaskTreeElem) {
    this.closeAddTaskForm();
    this.taskData = new AddEvent<TaskTreeElem>(task, false, true);
  }

  openEditTaskBelow(task: TaskTreeElem) {
    this.closeAddTaskForm();
    this.taskData = new AddEvent<TaskTreeElem>(task, true, false);
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

  // Context menu

  contextTaskMenu: TaskTreeElem | undefined;
  showContextTaskMenu: boolean = false;
  taskContextMenuX: number = 0;
  taskContextMenuY: number = 0;

  prepareContextMenu() {
    if (!this.blocked) {
      this.contextMenu.push(MenuElems.addTaskAbove);
      this.contextMenu.push(MenuElems.addTaskBelow);
      this.contextMenu.push(MenuElems.editTask);
      this.contextMenu.push(MenuElems.moveToProject);
      this.contextMenu.push(MenuElems.schedule);
      this.contextMenu.push(MenuElems.priority);
      this.contextMenu.push(MenuElems.duplicate);
      if(!this.inbox) {this.contextMenu.push(MenuElems.archiveTask);}
    }
    if (this.project && this.project.collaborative) {
      this.contextMenu.push(MenuElems.assign);
    }
    if (this.blocked) {
      this.contextMenu.push(MenuElems.restoreTask);
    }
    this.contextMenu.push(MenuElems.deleteTask);
  }

  openContextTaskMenu(event: MouseEvent, taskId: number) {
	  this.contextTaskMenu = this.getTaskById(taskId);
    this.showContextTaskMenu = true;
    this.taskContextMenuX = event.clientX;
    this.taskContextMenuY = event.clientY;
  }

  getTaskById(taskId: number): TaskTreeElem | undefined {
    return this.initTasks.length == 0 ? this.tree.getTaskById(taskId) : this.initTasks.find((a) => a.id == taskId);
  }

  closeContextMenu(code: number) {
    if(!this.contextTaskMenu) {return}
    let task = this.contextTaskMenu;
    this.closeContextTaskMenu();
    
    switch(code) {
      case(Codes.addTaskAbove): { this.openEditTaskAbove(task); break }
      case(Codes.addTaskBelow): { this.openEditTaskBelow(task); break }
      case(Codes.editTask): { this.openEditTaskForm(task); break }
      case(Codes.moveToProject): { this.openSelectProjectModal(task); break }
      case(Codes.schedule): { this.openSelectDateModal(task); break }
      case(Codes.priority): { this.openSelectPriorityModal(task); break }
      case(Codes.duplicate): { this.duplicate(task); break }
      case(Codes.archiveTask): { this.archiveTask(task); break }
      case(Codes.assign): { this.openAssignModal(task); break }
      case(Codes.restoreTask): { this.restoreTask(task); break }
      case(Codes.deleteTask): { this.askForDelete(task); break }
    }
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

  askForDelete(task: TaskTreeElem) {
    this.deleteService.openModalForTask(task);
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

  duplicate(task: TaskTreeElem) {
    let id: number = task.id;
    this.taskService.duplicateTask(id).subscribe(
        (response: TaskDetails[], mainId: number = id) => {
        this.tree.duplicateTask(response, mainId);
      },
      (error: HttpErrorResponse) => {
        
      }
    );
  }

  openTask?: TaskTreeElem;

  openTaskView(task: TaskTreeElem) {
    this.openTask = task;
  }

  closeTaskView() {
    this.openTask = undefined;
  }

  archiveTask(task: TaskTreeElem) {
    this.taskService.archiveTask(task.id, {flag:true}).subscribe(
        (response: Task) => {
        this.tree.archiveTask(response);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }

  restoreTask(task: TaskTreeElem) {
    this.taskService.archiveTask(task.id, {flag:false}).subscribe(
          (response: Task, tasks: TaskTreeElem[] = this.initTasks) => {
          this.tree.restoreTask(response ,tasks);
        },
        (error: HttpErrorResponse) => {
        
        }
     );
  }

  showAssignModal: boolean = false;
  taskIdForAssignation: number | undefined;

  assign(event: UserMin) {
    if(this.taskIdForAssignation) {
      this.taskService.updateAssigned({id: event.id}, this.taskIdForAssignation).subscribe(
          (response: Task, user: UserMin = event) => {
          this.tree.updateAssignation(response, user);
        },
        (error: HttpErrorResponse) => {
        
        }
     );
    }
    this.closeAssignModal();
  }

  openAssignModal(task: TaskTreeElem) {
    this.showAssignModal = true;
    this.taskIdForAssignation = task.id;
  }

  closeAssignModal() {
    this.showAssignModal = false;
  }
}
