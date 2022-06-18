import { Component, ElementRef, HostListener, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { FilterDetails } from './filter/dto/filter-details';
import { LabelDetails } from './label/dto/label-details';
import { ProjectTreeElem } from './project/dto/project-tree-elem';
import { AddEvent } from './common/utils/add-event';
import { DeleteService } from './utils/delete.service';
import { KeyboardManagerService } from './utils/keyboard-manager.service';
import { LoginService } from './user/login.service';
import { TreeService } from './utils/tree.service';
import { Animations } from './common/animations';

export interface SearchForm {
  search: FormControl<string>;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  animations: [Animations.menuTrigger]
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

  searchForm: FormGroup<SearchForm>;
  smallWindow: boolean = false;
  hideScroll: boolean = false;

  constructor(public tree : TreeService, public deleteService: DeleteService,
    private router: Router, private fb: FormBuilder, private keyboard: KeyboardManagerService,
    private loginService: LoginService) {
    this.searchForm = this.fb.nonNullable.group({
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

  openProjectModal(event: AddEvent<ProjectTreeElem>): void {
    this.displayProjectModal = true;
    this.projectData = event;
  }

  closeProjectModal(): void {
    this.displayProjectModal = false;
    this.projectData = undefined;
  }
  
  // List collapsion

  switchHideMenu(): void {
    this.hideMenu = !this.hideMenu;
  }

  // Navigation

  toHome(): void {
    this.router.navigate(['/']);
  }

  toInbox(): void {
    this.router.navigate(['/inbox']);
  }

  toUpcoming(): void {
    this.router.navigate(['/upcoming']);
  }
  
  toSettings(): void {
    this.router.navigate(['/settings']);
  }

  // Task modal window

  openAddTaskModal(): void {
    this.displayAddTask = true;
  }

  closeAddTaskModal(): void {
    this.displayAddTask = false;
  }

  displayLabelModal: boolean = false;
  labelData: AddEvent<LabelDetails> | undefined;

  openLabelModal(event: AddEvent<LabelDetails>): void {
    this.displayLabelModal = true;
    this.labelData = event;
  }

  closeLabelModal(): void {
    this.displayLabelModal = false;
    this.labelData = undefined;
  }

  displayFilterModal: boolean = false;
  filterData: AddEvent<FilterDetails> | undefined;

  openFilterModal(event: AddEvent<FilterDetails>): void {
    this.displayFilterModal = true;
    this.filterData = event;
  }

  closeFilterModal(): void {
    this.displayFilterModal = false;
    this.filterData = undefined;
  }

  search(): void {
    this.router.navigate(['/search'], { queryParams: {search: this.searchForm.controls.search.value}});
  }

  get modalOpened(): boolean {
    return this.displayAddTask || this.displayFilterModal || this.displayLabelModal || this.displayProjectModal || this.deleteService.showDeleteMonit;
  }

  // Listeners

  @HostListener('window:resize',['$event'])
  onWindowResize(): void {
    if(window.innerWidth <= 767) {
      this.smallWindow = true;
      this.hideMenu = true;
    } else {
      this.smallWindow = false;
      this.hideMenu = false;
    }
  }

  @ViewChild("searchInput") inputSearch?: ElementRef;
  keyboardNavActivated: boolean = false;

  @HostListener("window:keypress", ["$event"])
  handleKeyboardLetterEvent(event: KeyboardEvent): void {
    let letter: string = event.key;
    if(this.keyboard.inInputMode || this.modalOpened) {
      return;
    }
    if(this.keyboardNavActivated) {
      if(letter == 'i') {
        this.toInbox();
      } else if(letter == 't') {
        this.toHome();
      } else if(letter == 'u') {
        this.toUpcoming();
      } else if(letter == 's') {
        this.toSettings();
      }
      this.keyboardNavActivated = false;
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
    } else if(letter == 'g') {
      this.keyboardNavActivated = true;
    }
  }

  get logged(): boolean {
    return this.loginService.logged;
  }


  switchScroll(event: boolean) {
    this.hideScroll = event;
  }
}
