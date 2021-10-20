import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit, Output, EventEmitter, Renderer2, ViewChild, ElementRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LabelDetails } from 'src/app/entity/label-details';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { Task } from 'src/app/entity/task';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { ProjectService } from 'src/app/service/project.service';
import { TaskService } from 'src/app/service/task.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-task-form',
  templateUrl: './task-form.component.html',
  styleUrls: ['./task-form.component.css']
})
export class TaskFormComponent implements OnInit {
  @Output() closeEvent = new EventEmitter<boolean>();
  taskForm: FormGroup | undefined;
  showSelectProjectMenu: boolean = false;
  projects: ProjectTreeElem[] = [];
  showSelectDateMenu: boolean = false;
  taskDate: Date | undefined;

  projectSelectForm: FormGroup | undefined;
  @Input() project: ProjectTreeElem | undefined;

  task: TaskTreeElem | undefined;
  after: boolean = false;
  before: boolean = false;
  asChild: boolean = false;

  @Input() data: AddEvent<TaskTreeElem> | undefined;

  constructor(public tree: TreeService, private service: ProjectService, 
    private fb: FormBuilder, private taskService: TaskService) {  }

  ngOnInit(): void {
    if(this.data) {
      this.task = this.data.object;
      this.after = this.data.after;
      this.before = this.data.before;
      this.asChild =  this.data.asChild;
    }

    this.taskForm = this.fb.group({
      title: [this.task && !this.after && !this.before && !this.asChild ? this.task.title : '', Validators.required],
      description: [this.task && !this.after && !this.before && !this.asChild  ? this.task.description : '', []]
    });
    
    if(this.task && !this.after && !this.before ) {
		  this.taskDate = this.task.due ? this.task.due : undefined;
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

  dateWithinWeek(date: Date): boolean {
    let dateToCompare: Date = new Date();
    dateToCompare.setDate(dateToCompare.getDate() + 9);
    dateToCompare.setHours(0);
    dateToCompare.setMinutes(0);
    dateToCompare.setSeconds(0);
    dateToCompare.setMilliseconds(0);
    return date < dateToCompare && !this.isOverdue(date);
  }

  isOverdue(date: Date): boolean {
    let dateToCompare: Date = new Date();
    dateToCompare.setHours(0);
    dateToCompare.setMinutes(0);
    dateToCompare.setSeconds(0);
    return date < dateToCompare;
  }

  sameDay(date1: Date, date2: Date): boolean {
    return date1.getFullYear() == date2.getFullYear() && date1.getDate() == date2.getDate() && date1.getMonth() == date2.getMonth();
  }

  isToday(date: Date): boolean {
    let today = new Date();
    return this.sameDay(today, date);
  }

  isTomorrow(date: Date): boolean {
    let tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return this.sameDay(tomorrow, date);
  }

  thisYear(date: Date): boolean {
    let today = new Date();
    return today.getFullYear() == date.getFullYear();
  }

  openSelectDateMenu() {
    this.showSelectDateMenu = true;
  }

  closeSelectDateMenu(date: Date | undefined) {
    this.taskDate = date;
    this.showSelectDateMenu = false;
  }

  cancelDateSelection() {
    this.showSelectDateMenu = false;
  }

  closeForm() {
    this.closeEvent.emit(true);
  }

  save() {
    if(this.task) {
      this.makeRequestWithTask();
    } else {
      this.addTask();
    }

    this.closeEvent.emit(true);
  }
  
  addTask() {
    if(this.taskForm && this.taskForm.valid) {
      let lbls = this.labels.map((a) => a.id);
      this.service.addTask({
        title: this.taskForm.controls.title.value,
        description: this.taskForm.controls.description.value,
        projectOrder: 0,
        parentId: null,
        projectId: this.project ? this.project.id : null,
        priority: this.priority,
        due: this.taskDate ? this.taskDate : null,
        completedAt: null,
        labelIds: lbls
      }, this.project ? this.project.id : undefined).subscribe(
        (response: Task, projectId: number | undefined = this.project?.id, labels: number[] = lbls) => {
          this.tree.addNewTask(response, projectId, labels);
        },
        (error: HttpErrorResponse) => {
         
        }
      );
    }
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
    if(this.task && this.taskForm && this.taskForm.valid) {
      let lbls = this.labels.map((a) => a.id);
      this.taskService.updateTask({
        title: this.taskForm.controls.title.value,
        description: this.taskForm.controls.description.value,
        projectOrder: this.task.order,
        parentId: this.task.parent ? this.task.parent.id : null,
        projectId: this.project ? this.project.id : null,
        priority: this.priority,
        due: this.taskDate ? this.taskDate : null,
        completedAt: null,
        labelIds: lbls
      }, this.task.id).subscribe(
        (response: Task, projectId: number | undefined = this.project?.id, labels: number[] = lbls) => {
          this.tree.updateTask(response, projectId, labels);
        },
        (error: HttpErrorResponse) => {
         
        }
      );
    }
  }

  addTaskAfter() {
    if(this.task && this.taskForm && this.taskForm.valid) {
      let indentAfter = this.task.indent;
      let idAfter = this.task.id;
      let lbls = this.labels.map((a) => a.id);
      this.taskService.addTaskAfter({
        title: this.taskForm.controls.title.value,
        description: this.taskForm.controls.description.value,
        projectOrder: this.task.order,
        parentId: this.task.id,
        projectId: this.project ? this.project.id : null,
        priority: this.priority,
        due: this.taskDate ? this.taskDate : null,
        completedAt: null,
        labelIds: lbls
      }, this.task.id).subscribe(
        (response: Task, indent: number = indentAfter, taskId: number = idAfter, project: ProjectTreeElem | undefined = this.project, labels: number[] = lbls) => {
          this.tree.addNewTaskAfter(response, indent, taskId, project, labels);
        },
        (error: HttpErrorResponse) => {
         
        }
      );
    }
  }

  addTaskBefore() {
    if(this.task && this.taskForm && this.taskForm.valid) {
      let indentBefore = this.task.indent;
      let idBfore = this.task.id;
      let lbls = this.labels.map((a) => a.id);
      this.taskService.addTaskBefore({
        title: this.taskForm.controls.title.value,
        description: this.taskForm.controls.description.value,
        projectOrder: this.task.order,
        parentId: this.task.id,
        projectId: this.project ? this.project.id : null,
        priority: this.priority,
        due: this.taskDate ? this.taskDate : null,
        completedAt: null,
        labelIds: lbls
      }, this.task.id).subscribe(
        (response: Task, indent: number = indentBefore, taskId: number = idBfore, project: ProjectTreeElem | undefined = this.project, labels: number[] = lbls) => {
          this.tree.addNewTaskBefore(response, indent, taskId, project, labels);
        },
        (error: HttpErrorResponse) => {
         
        }
      );
    }
  }

  addTaskAsChild() {
    if(this.task && this.taskForm && this.taskForm.valid) {
      let indentChild = this.task.indent+1;
      let idParent = this.task.id;
      let lbls = this.labels.map((a) => a.id);
      this.taskService.addTaskAsChild({
        title: this.taskForm.controls.title.value,
        description: this.taskForm.controls.description.value,
        projectOrder: this.task.order,
        parentId: this.task.id,
        projectId: this.project ? this.project.id : null,
        priority: this.priority,
        due: this.taskDate ? this.taskDate : null,
        completedAt: null,
        labelIds: lbls
      }, this.task.id).subscribe(
        (response: Task, indent: number = indentChild, taskId: number = idParent, project: ProjectTreeElem | undefined = this.project, labels: number[] = lbls) => {
          this.tree.addNewTaskAsChild(response, indent, taskId, project, labels);
        },
        (error: HttpErrorResponse) => {
         
        }
      );
    }
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
