import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LabelDetails } from './entity/label-details';
import { ProjectTreeElem } from './entity/project-tree-elem';
import { AddEvent } from './entity/utils/add-event';
import { DeleteService } from './service/delete.service';
import { NotificationService } from './service/notification.service';
import { TreeService } from './service/tree.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'ladder';
  collapseFilters: boolean = true;
  hideMenu: boolean = false;

  addAfter: boolean = false;
  addBefore: boolean = false;
  projectForModalWindow: ProjectTreeElem | undefined;

  displayProjectModal: boolean = false;
  projectData: AddEvent<ProjectTreeElem> | undefined;

  displayAddTask: boolean = false;

  constructor(public tree : TreeService, public deleteService: DeleteService,
    private router: Router, private notifications: NotificationService) {
  }

  ngOnInit(): void {
    this.notifications.subscribe();
  }

  //Project modal window

  openProjectModal(event: AddEvent<ProjectTreeElem>) {
    this.displayProjectModal = true;
    this.projectData = event;
  }

  closeProjectModal() {
    this.displayProjectModal = false;
    this.projectData = undefined;
  }
  
  // List collapsion

  switchHideMenu() {
    this.hideMenu = !this.hideMenu;
  }

  // Navigation

  toHome() {
    this.router.navigate(['/']);
  }

  // Task modal window

  openAddTaskModal() {
    this.displayAddTask = true;
  }

  closeAddTaskModal() {
    this.displayAddTask = false;
  }

  displayLabelModal: boolean = false;
  labelData: AddEvent<LabelDetails> | undefined;

  openLabelModal(event: AddEvent<LabelDetails>) {
    this.displayLabelModal = true;
    this.labelData = event;
  }

  closeLabelModal() {
    this.displayLabelModal = false;
    this.labelData = undefined;
  }

  search() {
    this.router.navigate(['/search'], { queryParams: {search: '16-11-2021'}});
  }

}
