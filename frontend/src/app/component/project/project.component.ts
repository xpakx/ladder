import { HttpErrorResponse } from '@angular/common/http';
import { AfterViewInit, Component, ElementRef, OnInit, Renderer2, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DndDropEvent } from 'ngx-drag-drop';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { Task } from 'src/app/entity/task';
import { TaskDetails } from 'src/app/entity/task-details';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { ProjectService } from 'src/app/service/project.service';
import { TaskService } from 'src/app/service/task.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-project',
  templateUrl: './project.component.html',
  styleUrls: ['./project.component.css']
})
export class ProjectComponent implements OnInit, AfterViewInit {
  public invalid: boolean = false;
  public message: string = '';
  todayDate: Date | undefined;
  project: ProjectTreeElem | undefined;
  showAddTaskForm: boolean = false;
  showEditTaskFormById: number | undefined;
  id!: number;

  draggedId: number | undefined;

  constructor(private router: Router, private route: ActivatedRoute, 
    private tree: TreeService, private taskService: TaskService,
    private renderer: Renderer2) { }

  ngOnInit(): void {
    if(!this.tree.isLoaded()) {
      this.router.navigate(["load"]);
    }

    this.route.params.subscribe(routeParams => {
      this.loadProject(routeParams.id);
    });    
  }

  get tasks(): TaskTreeElem[] {
    return this.tree.getTasksByProject(this.id);
  }

  loadProject(id: number) {
    this.id = id;
    this.project = this.tree.getProjectById(id);
  }

  openAddTaskForm() {
    this.closeEditTaskForm();
    this.showAddTaskForm = true;
  }

  closeAddTaskForm() {
    this.showAddTaskForm = false;
  }

  openEditTaskForm(id: number) {
    this.closeAddTaskForm();
    this.showEditTaskFormById = id;
  }

  closeEditTaskForm() {
    this.showEditTaskFormById = undefined;
  }

  completeTask(id: number) {
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

  isParentCollapsed(tasks: TaskTreeElem[]): boolean {
    return tasks.find((a) => a.collapsed) ? true : false;
  }

  hideDropZone(task: TaskTreeElem): boolean {
    return this.isDragged(task.id) || 
    this.isParentDragged(task.parentList) || 
    this.isParentCollapsed(task.parentList);
  }

  onDrop(event: DndDropEvent, target: TaskTreeElem, asChild: boolean = false) {
    let id = Number(event.data);
    if(!asChild)
    {
      this.taskService.moveTaskAfter({id: target.id}, id).subscribe(
          (response: Task, indent: number = target.indent, afterId: number = target.id) => {
          this.tree.moveTaskAfter(response, indent, afterId);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    } else {
      this.taskService.moveTaskAsChild({id: target.id}, id).subscribe(
          (response: Task, indent: number = target.indent+1, afterId: number = target.id) => {
          this.tree.moveTaskAsChild(response, indent, afterId);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }

  onDropFirst(event: DndDropEvent) {
    alert(event.data + " on first item");
  }

  onDragStart(id: number) {
	  this.draggedId = id;
  }

  onDragEnd() {
	  this.draggedId = undefined;
  }

  isDragged(id: number): boolean {
    return this.draggedId == id;
  }

  isParentDragged(tasks: TaskTreeElem[]): boolean {
	  for(let task of tasks) {
      if(task.id == this.draggedId) {
        return true;
      }
	  }
	  return false;
  }
  
  collapseTask(taskId: number) {
    let task = this.tree.getTaskById(taskId);
    if(task) {
      task.collapsed = !task.collapsed;
      this.taskService.updateTaskCollapse(task.id, {flag: task.collapsed}).subscribe(
        (response: Task) => {
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }
  
  isTaskCollapsed(taskId: number): boolean {
    let task = this.tree.getTaskById(taskId);
    if(task) {
      return task.collapsed ? true : false;
    }
	  return false;
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
	  this.contextTaskMenu = this.tree.getTaskById(taskId);
    this.showContextTaskMenu = true;
    this.contextTaskMenuJustOpened = true;
    this.taskContextMenuX = event.clientX-250;
    this.taskContextMenuY = event.clientY;
  }

  closeContextTaskMenu() {
    this.contextTaskMenu = undefined;
    this.showContextTaskMenu = false;
  }

  getNumOfUncompletedTasksByParent(parentId: number): number {
    return this.tree.getNumOfUncompletedTasksByParent(parentId);
  }

  getNumOfTasksByParent(parentId: number): number {
    return this.tree.getNumOfTasksByParent(parentId);
  }

  openEditTaskFromContextMenu() {
    if(this.contextTaskMenu) {
      this.openEditTaskForm(this.contextTaskMenu.id);
    }
    this.closeContextTaskMenu();
  }

  deleteTask() {
    if(this.contextTaskMenu) {
      let deletedTaskId: number = this.contextTaskMenu.id;
      this.taskService.deleteTask(deletedTaskId).subscribe(
        (response: any, taskId: number = deletedTaskId) => {
        this.tree.deleteTask(taskId);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
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

}
