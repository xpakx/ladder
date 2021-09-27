import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
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
export class ProjectComponent implements OnInit {
  public invalid: boolean = false;
  public message: string = '';
  tasks: TaskTreeElem[] = [];
  todayDate: Date | undefined;
  project: ProjectTreeElem | undefined;
  showAddTaskForm: boolean = false;
  showEditTaskFormById: number | undefined;

  draggedId: number | undefined;

  constructor(private router: Router, private route: ActivatedRoute, 
    private tree: TreeService, private taskService: TaskService) { }

  ngOnInit(): void {
    if(!this.tree.isLoaded()) {
      this.router.navigate(["load"]);
    }

    this.route.params.subscribe(routeParams => {
      this.loadProject(routeParams.id);
    });    
  }

  loadProject(id: number) {
    this.project = this.tree.getProjectById(id);
    this.tasks = this.tree.getTasksByProject(id);
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

}
