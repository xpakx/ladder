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
  nextDates: Date[] = [];
  @ViewChildren(TaskDailyListComponent) listComponents!: TaskDailyListComponent[];
  tasks: TaskTreeElem[][] = [];

  constructor(private router: Router, public tree: TreeService, 
    private taskService: TaskService, private taskTreeService: TaskTreeService) {}

  ngOnInit(): void {
    if(!this.tree.isLoaded()) {
      this.router.navigate(["load"]);
    }

    this.todayDate = new Date();
    this.nextDates = [];
    for(let i=1;i<7;i++) {
      let newDate = new Date(this.todayDate);
      newDate.setDate(newDate.getDate()+i)
      this.nextDates.push(newDate)
    }
    this.tasks = this.nextDates.map((a) => this.tree.getByDate(a));
  }

  get overdue(): TaskTreeElem[] {
    return this.tree.getByDateOverdue(this.todayDate);
  }

  showAddTaskForm: number = -1;

  closeAddTaskForm() {
    this.showAddTaskForm = -1;
  }

  openAddTaskForm(id: number) {
    this.showAddTaskForm = id;
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
