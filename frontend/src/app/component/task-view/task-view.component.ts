import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Task } from 'src/app/entity/task';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { TaskTreeService } from 'src/app/service/task-tree.service';
import { TaskService } from 'src/app/service/task.service';

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

  constructor(private taskTree: TaskTreeService, private taskService: TaskService) { }

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
}
