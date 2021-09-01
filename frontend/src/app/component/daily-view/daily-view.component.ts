import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FullProjectTree } from 'src/app/entity/full-project-tree';
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

  constructor(private router: Router, private tree: TreeService,
    private service: ProjectService) { }

  ngOnInit(): void {
    
  }

}
