import { Component, ElementRef, HostListener, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { FilterDetails } from './entity/filter-details';
import { LabelDetails } from './entity/label-details';
import { ProjectTreeElem } from './entity/project-tree-elem';
import { AddEvent } from './entity/utils/add-event';
import { DeleteService } from './service/delete.service';
import { KeyboardManagerService } from './service/keyboard-manager.service';
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

  searchForm: FormGroup;
  smallWindow: boolean = false;

  constructor(public tree : TreeService, public deleteService: DeleteService,
    private router: Router, private fb: FormBuilder, private keyboard: KeyboardManagerService) {
      this.searchForm = this.fb.group({
        search: ['']
      });
  }

  ngOnInit(): void {
    if(window.innerWidth <= 767) {
      this.smallWindow = true;
      this.hideMenu = true;
    }
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
  
  toSettings() {
    this.router.navigate(['/settings']);
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

  displayFilterModal: boolean = false;
  filterData: AddEvent<FilterDetails> | undefined;

  openFilterModal(event: AddEvent<FilterDetails>) {
    this.displayFilterModal = true;
    this.filterData = event;
  }

  closeFilterModal() {
    this.displayFilterModal = false;
    this.filterData = undefined;
  }

  search() {
    this.router.navigate(['/search'], { queryParams: {search: this.searchForm.controls.search.value}});
  }

  get modalOpened(): boolean {
    return this.displayAddTask || this.displayFilterModal || this.displayLabelModal || this.displayProjectModal || this.deleteService.showDeleteMonit;
  }

  // Listeners

  @HostListener('window:resize',['$event'])
  onWindowResize() {
    if(window.innerWidth <= 767) {
      this.smallWindow = true;
      this.hideMenu = true;
    } else {
      this.smallWindow = false;
      this.hideMenu = false;
    }
  }


  @ViewChild("searchInput") inputSearch?: ElementRef;

  @HostListener("window:keypress", ["$event"])
    handleKeyboardLetterEvent(event: KeyboardEvent) {
      let letter: string = event.key;
      if(this.keyboard.inInputMode || this.modalOpened) {
        return;
      }
      if(letter == 'q') {
        this.openAddTaskModal();
      } else if(letter == 'm') {
        this.hideMenu = !this.hideMenu;
      } else if(letter == '/') {
        event.preventDefault();
        this.inputSearch?.nativeElement.focus();
        this.inputSearch?.nativeElement.select();
      }
  }
}
