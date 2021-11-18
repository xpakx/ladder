import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { Router } from '@angular/router';
import { FilterDetails } from 'src/app/entity/filter-details';
import { LabelDetails } from 'src/app/entity/label-details';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  collapseFilters: boolean = true;
  hideMenu: boolean = false;

  @Output() projectEvent = new EventEmitter<AddEvent<ProjectTreeElem>>();
  @Output() labelEvent = new EventEmitter<AddEvent<LabelDetails>>();
  @Output() filterEvent = new EventEmitter<AddEvent<FilterDetails>>();

  constructor(private router: Router, public tree : TreeService) { }

  ngOnInit(): void {
  }

  // Navigation

  toHome() {
    this.router.navigate(['/']);
  }

  toInbox() {
    this.router.navigate(['/inbox']);
  }

  toUpcoming() {
    this.router.navigate(['/upcoming']);
  }

  // List collapsion

  switchFilterCollapse() {
    this.collapseFilters = !this.collapseFilters;
  }

  openProjectModal(event: AddEvent<ProjectTreeElem>) {
    this.projectEvent.emit(event);
  }

  openLabelModal(event: AddEvent<LabelDetails>) {
    this.labelEvent.emit(event);
  }

  openFilterModal(event: AddEvent<FilterDetails>) {
    this.filterEvent.emit(event);
  }
}
