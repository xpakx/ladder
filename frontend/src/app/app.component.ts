import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { LabelDetails } from './entity/label-details';
import { ProjectTreeElem } from './entity/project-tree-elem';
import { ProjectService } from './service/project.service';
import { TaskService } from './service/task.service';
import { TreeService } from './service/tree.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'ladder';
  collapseFilters: boolean = true;
  hideMenu: boolean = false;

  addAfter: boolean = false;
  addBefore: boolean = false;
  projectForModalWindow: ProjectTreeElem | undefined;
  displayProjectModal: boolean = false;

  displayAddTask: boolean = false;

  constructor(public tree : TreeService, private projectService: ProjectService, 
    private router: Router, private taskService: TaskService) {
  }

  //Project modal window

  openProjectModal() {
    this.displayProjectModal = true;
  }

  openProjectModalWithProject(project: ProjectTreeElem | undefined) {
    this.projectForModalWindow = project;
    this.openProjectModal();
  }

  openProjectModalAbove(project: ProjectTreeElem | undefined) {
    this.addBefore = true;
    this.openProjectModalWithProject(project);
  }

  openProjectModalBelow(project: ProjectTreeElem | undefined) {
    this.addAfter = true;
    this.openProjectModalWithProject(project);
  }

  closeProjectModal() {
    this.displayProjectModal = false;
    this.projectForModalWindow = undefined;
    this.addAfter = false;
    this.addBefore = false;
  }

  // List collapsion

  switchFilterCollapse() {
    this.collapseFilters = !this.collapseFilters;
  }

  switchHideMenu() {
    this.hideMenu = !this.hideMenu;
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

  // Task modal window

  openAddTaskModal() {
    this.displayAddTask = true;
  }

  closeAddTaskModal() {
    this.displayAddTask = false;
  }

  displayLabelModal: boolean = false;

  openLabelModal() {
    this.displayLabelModal = true;
  }

  closeLabelModal() {
    this.displayLabelModal = false;
  }


  labelForModalWindow: LabelDetails | undefined;

  openLabelModalWithLabel(label: LabelDetails | undefined) {
    this.labelForModalWindow = label;
    this.openLabelModal();
  }
}
