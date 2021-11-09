import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Habit } from 'src/app/entity/habit';
import { LabelDetails } from 'src/app/entity/label-details';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { HabitService } from 'src/app/service/habit.service';
import { ProjectService } from 'src/app/service/project.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-habit-form',
  templateUrl: './habit-form.component.html',
  styleUrls: ['./habit-form.component.css']
})
export class HabitFormComponent implements OnInit {
  @Output() closeEvent = new EventEmitter<boolean>();
  habitForm: FormGroup | undefined;
  showSelectProjectMenu: boolean = false;
  projects: ProjectTreeElem[] = [];

  projectSelectForm: FormGroup | undefined;
  @Input() project: ProjectTreeElem | undefined;

  task: TaskTreeElem | undefined;
  after: boolean = false;
  before: boolean = false;
  asChild: boolean = false;
  allowPositive: boolean = true;
  allowNegative: boolean = false;

  @Input() data: AddEvent<TaskTreeElem> | undefined;

  constructor(public tree: TreeService, private service: ProjectService, 
    private fb: FormBuilder, private habitService: HabitService) {  }

  get valid() {
    return this.habitForm && this.habitForm.valid && (this.allowNegative || this.allowPositive);
  }

  ngOnInit(): void {
    if(this.data) {
      this.task = this.data.object;
      this.after = this.data.after;
      this.before = this.data.before;
      this.asChild =  this.data.asChild;
    }

    this.habitForm = this.fb.group({
      title: [this.task && !this.after && !this.before && !this.asChild ? this.task.title : '', Validators.required],
      description: [this.task && !this.after && !this.before && !this.asChild  ? this.task.description : '', []]
    });
    
    if(this.task && !this.after && !this.before ) {
      this.priority = this.task.priority;
      this.labels = this.task.labels;
      this.project = this.task.project ? this.tree.getProjectById(this.task.project.id) : undefined;
	  }
  }

  openSelectProjectMenu() {
    this.showSelectProjectMenu = true;
  }

  closeSelectProjectMenu() {
    this.showSelectProjectMenu = false;
  }

  chooseProject(project: ProjectTreeElem | undefined) {
    this.closeSelectProjectMenu();
    this.project = project;
    this.after = false;
    this.before = false;
  }

  closeForm() {
    this.closeEvent.emit(true);
  }

  save() {
    if(this.task) {
      this.makeRequestWithTask();
    } else {
      this.addHabit();
    }

    this.closeEvent.emit(true);
  }
  
  addHabit() {
    if(this.habitForm && this.habitForm.valid) {
      let lbls = this.labels.map((a) => a.id);
      this.habitService.addHabit({
        title: this.habitForm.controls.title.value,
        description: this.habitForm.controls.description.value,
        allowPositive: this.allowPositive,
        allowNegative: this.allowNegative
      }, this.project ? this.project.id : undefined).subscribe(
        (response: Habit, projectId: number | undefined = this.project?.id, labels: number[] = lbls) => {
          this.tree.addNewHabit(response, projectId, labels);
        },
        (error: HttpErrorResponse) => {
         
        }
      );
    }
  }

  switchAllowPositive() {
    this.allowPositive = !this.allowPositive;
  }

  switchAllowNegative() {
    this.allowNegative = !this.allowNegative;
  }

  makeRequestWithTask() {
    if(this.asChild) {
      this.addTaskAsChild();
    } else if(this.before) {
      this.addTaskBefore();
    } else if(this.after) {
      this.addTaskAfter();
    } else {
      this.updateTask();
    }
  }

  updateTask() {
    
  }

  addTaskAfter() {
    
  }

  addTaskBefore() {
    
  }

  addTaskAsChild() {
    
  }

  priority: number = 0;
  priorityForModal: number = 0;
  showSelectPriorityMenu: boolean = false;

  openSelectPriorityMenu() {
    this.priorityForModal = this.priority;
    this.showSelectPriorityMenu = true;
  }

  closeSelectPriorityMenu() {
    this.showSelectPriorityMenu = false;
  }

  choosePriority(priority: number) {
    this.closeSelectPriorityMenu();
    this.priority = priority;
  }

  labels: LabelDetails[] = [];
  labelsForModal: LabelDetails[] = [];
  showSelectLabelsMenu: boolean = false;

  openSelectLabelsMenu() {
    this.labelsForModal = [...this.labels];
    this.showSelectLabelsMenu = true;
  }

  closeSelectLabelsMenu() {
    this.showSelectLabelsMenu = false;
  }

  chooseLabel(labels: LabelDetails[]) {
    this.closeSelectLabelsMenu();
    this.labels = labels;
  }

}
