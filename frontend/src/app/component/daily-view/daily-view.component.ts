import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FullProjectTree } from 'src/app/entity/full-project-tree';
import { TaskDetails } from 'src/app/entity/task-details';
import { TaskWithChildren } from 'src/app/entity/task-with-children';
import { ProjectService } from 'src/app/service/project.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-daily-view',
  templateUrl: './daily-view.component.html',
  styleUrls: ['./daily-view.component.css']
})
export class DailyViewComponent implements OnInit {
  public invalid: boolean = false;
  public message: string = '';
  tasks: TaskDetails[] = [];
  todayDate: Date | undefined;

  constructor(private router: Router, private tree: TreeService,
    private service: ProjectService) { }

  ngOnInit(): void {
    this.todayDate = new Date();
    this.tasks = this.tree.getByDate(this.todayDate);
  }

}
