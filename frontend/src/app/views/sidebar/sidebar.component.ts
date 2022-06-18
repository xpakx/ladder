import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { Router } from '@angular/router';
import { FilterDetails } from 'src/app/filter/dto/filter-details';
import { LabelDetails } from 'src/app/label/dto/label-details';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { AddEvent } from 'src/app/common/utils/add-event';
import { TreeService } from 'src/app/utils/tree.service';

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
  @Output() closeEvent = new EventEmitter<boolean>();
  @Output("hideScroll") hideScrollEvent = new EventEmitter<boolean>();

  @Input() mobile: boolean = false;

  constructor(private router: Router, public tree : TreeService) { }

  ngOnInit(): void {
  }

  // Navigation
  mobileNav() {
    if (this.mobile) {
      this.closeEvent.emit(true);
    }
  }

  toHome() {
    this.router.navigate(['/']);
    this.mobileNav();
  }

  toInbox() {
    this.router.navigate(['/inbox']);
    this.mobileNav();
  }

  toUpcoming() {
    this.router.navigate(['/upcoming']);
    this.mobileNav();
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

  contextMenuOpened: boolean[] = [false, false, false, false];

  switchScroll(event: boolean, menu: number) {
    this.contextMenuOpened[menu] = event;
    this.hideScrollEvent.emit(this.contextMenuOpened.includes(true));
  }
}
