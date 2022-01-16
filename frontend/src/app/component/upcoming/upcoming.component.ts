import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChildren } from '@angular/core';
import { Router } from '@angular/router';
import { Task } from 'src/app/entity/task';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { TaskTreeService } from 'src/app/service/task-tree.service';
import { TaskService } from 'src/app/service/task.service';
import { TreeService } from 'src/app/service/tree.service';
import { TaskDailyListComponent } from '../task-daily-list/task-daily-list.component';

@Component({
  selector: 'app-upcoming',
  templateUrl: './upcoming.component.html',
  styleUrls: ['./upcoming.component.css']
})
export class UpcomingComponent implements OnInit {
  public invalid: boolean = false;
  public message: string = '';
  todayDate: Date = new Date();
  @ViewChildren(TaskDailyListComponent) listComponents!: TaskDailyListComponent[];

  constructor(private router: Router, public tree: TreeService, 
    private taskService: TaskService, private taskTreeService: TaskTreeService,) {}

  ngOnInit(): void {
    if(!this.tree.isLoaded()) {
      this.router.navigate(["upcoming"]);
    }

    this.todayDate = new Date();
  }

  get overdue(): TaskTreeElem[] {
    return this.tree.getByDateOverdue(this.todayDate);
  }

  showAddTaskForm: boolean = false;

  closeAddTaskForm() {
    this.showAddTaskForm = false;
  }


  showSelectDateModal: boolean = false;

  closeSelectDateModal(date: Date | undefined) {
    this.showSelectDateModal = false;
    this.taskService.rescheduleOverdueTasks({date: date}).subscribe(
      (response: Task[]) => {
        this.taskTreeService.updateTasksDate(response);
    },
    (error: HttpErrorResponse) => {
    
    });
  }

  cancelDateSelection() {
    this.showSelectDateModal = false;
  }

  openSelectDateModal() {
    this.showSelectDateModal = true;
  }
}
