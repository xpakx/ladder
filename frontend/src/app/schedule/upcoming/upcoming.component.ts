import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChildren } from '@angular/core';
import { Router } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import { Task } from 'src/app/task/dto/task';
import { TaskTreeElem } from 'src/app/task/dto/task-tree-elem';
import { DateEvent } from 'src/app/common/utils/date-event';
import { Day } from 'src/app/common/utils/day';
import { TaskTreeService } from 'src/app/task/task-tree.service';
import { TaskService } from 'src/app/task/task.service';
import { TreeService } from 'src/app/utils/tree.service';
import { TaskDailyListComponent } from 'src/app/task/task-daily-list/task-daily-list.component';

@Component({
  selector: 'app-upcoming',
  templateUrl: './upcoming.component.html',
  styleUrls: ['./upcoming.component.css']
})
export class UpcomingComponent implements OnInit {
  public invalid: boolean = false;
  public message: string = '';
  todayDate: Date = new Date();
  datesToShow: Date[] = [];
  @ViewChildren(TaskDailyListComponent) listComponents!: TaskDailyListComponent[];
  tasks: Day[] = [];
  mySub: Subscription;

  activateDragNDrop: boolean = false;

  constructor(private router: Router, public tree: TreeService, 
    private taskService: TaskService, private taskTreeService: TaskTreeService) {
      this.mySub = interval(500).subscribe((func => {
        this.refreshTasks();
      }))
    }

  ngOnInit(): void {
    if(!this.tree.isLoaded()) {
      this.router.navigate(["load"]);
    }

    this.todayDate = new Date();
    this.datesToShow = [];
    this.datesToShow.push(this.todayDate);
    for(let i=1;i<7;i++) {
      let newDate = new Date(this.todayDate);
      newDate.setDate(newDate.getDate()+i)
      this.datesToShow.push(newDate)
    }
    this.tasks = this.datesToShow.map((a, index) => {return {date: a, tasks: this.tree.getByDate(a), id: index, collapsed: false}});
  }

  refreshTasks() {
    let newTasks = this.datesToShow.map((a, index) => {return {date: a, tasks: this.tree.getByDate(a), id: index}});
    for(let day of newTasks) {
      this.tasks[day.id].tasks = day.tasks.sort((a,b) => a.dailyOrder - b.dailyOrder);
    }
  }

  nextPage() {
    let startDate = new Date(this.datesToShow[this.datesToShow.length-1]);
    this.datesToShow = [];
    for(let i=1;i<=7;i++) {
      let newDate = new Date(startDate);
      newDate.setDate(newDate.getDate()+i)
      this.datesToShow.push(newDate)
    }
    this.tasks = this.datesToShow.map((a, index) => {return {date: a, tasks: this.tree.getByDate(a), id: index, collapsed: false}});
  }

  prevPage() {
    let startDate = new Date(this.datesToShow[0]);
    this.datesToShow = [];
    startDate.setDate(startDate.getDate()-8);
    for(let i=1;i<=7;i++) {
      let newDate = new Date(startDate);
      newDate.setDate(newDate.getDate()+i)
      this.datesToShow.push(newDate)
    }
    this.tasks = this.datesToShow.map((a, index) => {return {date: a, tasks: this.tree.getByDate(a), id: index, collapsed: false}});
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

  closeSelectDateModal(event: DateEvent) {
    let date: Date | undefined = event.date;
    this.showSelectDateModal = false;
    this.taskService.rescheduleOverdueTasks({date: date, timeboxed: event.timeboxed}).subscribe(
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

  onDragStart() {
    this.activateDragNDrop = true;
  }

  onDragEnd() {
    this.activateDragNDrop = false;
  }

  collapse(day: Day) {
    day.collapsed = !day.collapsed;
  }
}
