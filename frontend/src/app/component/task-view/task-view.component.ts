import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { Task } from 'src/app/entity/task';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { DeleteService } from 'src/app/service/delete.service';
import { TaskTreeService } from 'src/app/service/task-tree.service';
import { TaskService } from 'src/app/service/task.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-task-view',
  templateUrl: './task-view.component.html',
  styleUrls: ['./task-view.component.css']
})
export class TaskViewComponent implements OnInit {
  @Input("task") parent?: TaskTreeElem;
  @Output() closeEvent = new EventEmitter<boolean>();
  edit: boolean = false;
  parentData!: AddEvent<TaskTreeElem>;

  constructor(private taskTree: TaskTreeService, private taskService: TaskService,
    private tree: TreeService, private deleteService: DeleteService) { }

  ngOnInit(): void {
    if(parent) {
      this.parentData = new AddEvent<TaskTreeElem>(this.parent);
    }
  }

  closeModal() {
    this.closeEvent.emit(true);
  }

  closeEditTaskForm() {
    this.edit = false;
  }

  complete() {
    if(this.parent) {
    this.taskService.completeTask(this.parent.id, {flag: !this.parent.completed}).subscribe(
        (response: Task) => {
        this.taskTree.changeTaskCompletion(response);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
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

  showSelectDateModal:boolean = false;

  openSelectDateModal() {
    this.showSelectDateModal = true;
  }

  closeSelectDateModal(date: Date | undefined) {
    this.showSelectDateModal = false;
    if(this.parent) {
      this.taskService.updateTaskDueDate({date: date}, this.parent.id).subscribe(
          (response: Task) => {
          this.taskTree.updateTaskDate(response);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
    this.showSelectDateModal = false;
  }
  
  cancelDateSelection() {
    this.showSelectDateModal = false;
  }

  showSelectProjectModal: boolean = false;
  projectForProjectModal: ProjectTreeElem | undefined;

  closeSelectProjectModal(project: ProjectTreeElem | undefined) {
    this.showSelectProjectModal = false;
    if(this.parent) {
      this.taskService.updateTaskProject({id: project? project.id : undefined}, this.parent.id).subscribe(
          (response: Task, proj: ProjectTreeElem | undefined = project) => {
          this.taskTree.moveTaskToProject(response, proj);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }

  cancelProjectSelection() {
    this.showSelectProjectModal = false;
  }

  openSelectProjectModal() {
    if(this.parent) {
      let project = this.parent.project ? this.tree.getProjectById(this.parent.project.id) : undefined;
      this.projectForProjectModal = project;
      this.showSelectProjectModal = true;
    }
  }

  askForDelete() {
    if(this.parent) {
      this.deleteService.openModalForTask(this.parent);
    }
  }

  showSelectPriorityModal: boolean = false;
  priorityForPriorityModal: number = 0;

  closeSelectPriorityModal(priority: number) {
    this.showSelectPriorityModal = false;
    if(this.parent) {
      this.taskService.updateTaskPriority({priority: priority}, this.parent.id).subscribe(
          (response: Task) => {
          this.tree.updateTaskPriority(response);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }

  cancelPrioritySelection() {
    this.showSelectPriorityModal = false;
  }

  openSelectPriorityModal() {
    if(this.parent) {
      this.priorityForPriorityModal = this.parent.priority;
      this.showSelectPriorityModal = true;
    }
  }
}
