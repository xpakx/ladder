import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Task } from 'src/app/entity/task';
import { TaskDetails } from 'src/app/entity/task-details';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { TaskService } from 'src/app/service/task.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-daily-view',
  templateUrl: './daily-view.component.html',
  styleUrls: ['./daily-view.component.css']
})
export class DailyViewComponent implements OnInit {
  public invalid: boolean = false;
  public message: string = '';
  tasks: TaskTreeElem[] = [];
  todayDate: Date | undefined;

  constructor(private router: Router, private tree: TreeService, private taskService: TaskService) { }

  ngOnInit(): void {
    if(!this.tree.isLoaded()) {
      this.router.navigate(["load"]);
    }

    this.todayDate = new Date();
    this.tasks = this.tree.getByDate(this.todayDate);
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

}
