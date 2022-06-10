import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit, Output, EventEmitter, HostListener } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Habit } from '../dto/habit';
import { HabitDetails } from '../dto/habit-details';
import { LabelDetails } from '../../label/dto/label-details';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { AddEvent } from 'src/app/common/utils/add-event';
import { HabitService } from 'src/app/habit/habit.service';
import { ProjectService } from 'src/app/project/project.service';
import { TreeService } from 'src/app/utils/tree.service';

export interface HabitForm {
  title: FormControl<string>;
  description: FormControl<string>;
}

@Component({
  selector: 'app-habit-form',
  templateUrl: './habit-form.component.html',
  styleUrls: ['./habit-form.component.css']
})
export class HabitFormComponent implements OnInit {
  @Output() closeEvent = new EventEmitter<boolean>();
  habitForm: FormGroup<HabitForm> | undefined;
  showSelectProjectMenu: boolean = false;
  projects: ProjectTreeElem[] = [];

  @Input() project: ProjectTreeElem | undefined;

  habit: HabitDetails | undefined;
  after: boolean = false;
  before: boolean = false;
  asChild: boolean = false;
  allowPositive: boolean = true;
  allowNegative: boolean = false;

  @Input() data: AddEvent<HabitDetails> | undefined;

  constructor(public tree: TreeService, private service: ProjectService, 
    private fb: FormBuilder, private habitService: HabitService) {  }

  get valid() {
    return this.habitForm && this.habitForm.valid && (this.allowNegative || this.allowPositive);
  }

  ngOnInit(): void {
    if(this.data) {
      this.habit = this.data.object;
      this.after = this.data.after;
      this.before = this.data.before;
      this.asChild =  this.data.asChild;
    }

    this.habitForm = this.fb.nonNullable.group({
      title: [this.habit && !this.after && !this.before && !this.asChild ? this.habit.title : '', Validators.required],
      description: [this.habit && !this.after && !this.before && !this.asChild  ? this.habit.description : '', []]
    });
    
    if(this.habit && !this.after && !this.before ) {
      this.priority = this.habit.priority;
      this.labels = this.habit.labels;
      this.allowPositive = this.habit.allowPositive;
      this.allowNegative = this.habit.allowNegative;
      this.project = this.habit.project ? this.tree.getProjectById(this.habit.project.id) : undefined;
	  }
  }

  openSelectProjectMenu(): void {
    this.showSelectProjectMenu = true;
  }

  closeSelectProjectMenu(): void {
    this.showSelectProjectMenu = false;
  }

  chooseProject(project: ProjectTreeElem | undefined): void {
    this.closeSelectProjectMenu();
    this.project = project;
    this.after = false;
    this.before = false;
  }

  closeForm(): void {
    this.closeEvent.emit(true);
  }

  save(): void {
    if(this.habit) {
      this.makeRequestWithTask();
    } else {
      this.addHabit();
    }

    this.closeEvent.emit(true);
  }
  
  addHabit(): void {
    if(this.habitForm && this.habitForm.valid) {
      let lbls = this.labels.map((a) => a.id);
      this.habitService.addHabit({
        title: this.habitForm.controls.title.value,
        description: this.habitForm.controls.description.value,
        allowPositive: this.allowPositive,
        allowNegative: this.allowNegative,
        projectId: this.project?.id,
        priority: this.priority,
        labelIds: lbls
      }, this.project ? this.project.id : undefined).subscribe(
        (response: Habit, projectId: number | undefined = this.project?.id, labels: number[] = lbls) => {
          this.tree.addNewHabit(response, projectId, labels);
        },
        (error: HttpErrorResponse) => {
         
        }
      );
    }
  }

  switchAllowPositive(): void {
    this.allowPositive = !this.allowPositive;
  }

  switchAllowNegative(): void {
    this.allowNegative = !this.allowNegative;
  }

  makeRequestWithTask(): void {
    if(this.asChild) {
      this.addTaskAsChild();
    } else if(this.before) {
      this.addTaskBefore();
    } else if(this.after) {
      this.addTaskAfter();
    } else {
      this.updateHabit();
    }
  }

  updateHabit(): void {
    if(this.habit && this.habitForm && this.habitForm.valid) {
      let lbls = this.labels.map((a) => a.id);
      this.habitService.updateHabit({
        title: this.habitForm.controls.title.value,
        description: this.habitForm.controls.description.value,
        allowPositive: this.allowPositive,
        allowNegative: this.allowNegative,
        projectId: this.project?.id,
        priority: this.priority,
        labelIds: lbls
      }, this.habit.id).subscribe(
        (response: Habit, projectId: number | undefined = this.project?.id, labels: number[] = lbls) => {
          this.tree.updateHabit(response, projectId, labels);
        },
        (error: HttpErrorResponse) => {
         
        }
      );
    }
  }

  addTaskAfter(): void {
    if(this.habit && this.habitForm && this.habitForm.valid) {
      let idAfter = this.habit.id;
      let lbls = this.labels.map((a) => a.id);
      this.habitService.addHabitAfter({
        title: this.habitForm.controls.title.value,
        description: this.habitForm.controls.description.value,
        allowPositive: this.allowPositive,
        allowNegative: this.allowNegative,
        projectId: this.project?.id,
        priority: this.priority,
        labelIds: lbls
      }, this.habit.id).subscribe(
        (response: Habit, habitId: number = idAfter, project: ProjectTreeElem | undefined = this.project, labels: number[] = lbls) => {
          this.tree.addNewHabitAfter(response, habitId, project, labels);
        },
        (error: HttpErrorResponse) => {
         
        }
      );
    }
  }

  addTaskBefore(): void {
    if(this.habit && this.habitForm && this.habitForm.valid) {
      let idBefore = this.habit.id;
      let lbls = this.labels.map((a) => a.id);
      this.habitService.addHabitBefore({
        title: this.habitForm.controls.title.value,
        description: this.habitForm.controls.description.value,
        allowPositive: this.allowPositive,
        allowNegative: this.allowNegative,
        projectId: this.project?.id,
        priority: this.priority,
        labelIds: lbls
      }, this.habit.id).subscribe(
        (response: Habit, habitId: number = idBefore, project: ProjectTreeElem | undefined = this.project, labels: number[] = lbls) => {
          this.tree.addNewHabitBefore(response, habitId, project, labels);
        },
        (error: HttpErrorResponse) => {
         
        }
      );
    }
  }

  addTaskAsChild(): void {
    
  }

  priority: number = 0;
  priorityForModal: number = 0;
  showSelectPriorityMenu: boolean = false;

  openSelectPriorityMenu(): void {
    this.priorityForModal = this.priority;
    this.showSelectPriorityMenu = true;
  }

  closeSelectPriorityMenu(): void {
    this.showSelectPriorityMenu = false;
  }

  choosePriority(priority: number): void {
    this.closeSelectPriorityMenu();
    this.priority = priority;
  }

  labels: LabelDetails[] = [];
  labelsForModal: LabelDetails[] = [];
  showSelectLabelsMenu: boolean = false;

  openSelectLabelsMenu(): void {
    this.labelsForModal = [...this.labels];
    this.showSelectLabelsMenu = true;
  }

  closeSelectLabelsMenu(): void {
    this.showSelectLabelsMenu = false;
  }

  chooseLabel(labels: LabelDetails[]): void {
    this.closeSelectLabelsMenu();
    this.labels = labels;
  }

  get subModalOpened(): boolean {
    return this.showSelectLabelsMenu || this.showSelectPriorityMenu || this.showSelectProjectMenu;
  }

  @HostListener("window:keydown.escape", ["$event"])
  handleKeyboardEscapeEvent(): void {
    if(!this.subModalOpened) {
      this.closeForm();
    }
  }

  @HostListener("window:keydown.enter", ["$event"])
  handleKeyboardEnterEvent(): void {
    if(!this.subModalOpened && this.habitForm?.valid) {
      this.save();
    }
  }
}
