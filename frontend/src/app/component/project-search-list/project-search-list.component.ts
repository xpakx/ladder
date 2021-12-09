import { Component, Input, OnInit } from '@angular/core';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';

@Component({
  selector: 'app-project-search-list',
  templateUrl: './project-search-list.component.html',
  styleUrls: ['./project-search-list.component.css']
})
export class ProjectSearchListComponent implements OnInit {
  @Input("projectList") projectList: ProjectTreeElem[] = [];

  constructor() { }

  ngOnInit(): void {
  }

}
