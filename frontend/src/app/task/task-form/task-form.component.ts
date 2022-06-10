import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit, Output, EventEmitter, HostListener } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { LabelDetails } from '../../label/dto/label-details';
import { ProjectTreeElem } from '../../project/dto/project-tree-elem';
import { Task } from 'src/app/task/dto/task';
import { TaskTreeElem } from 'src/app/task/dto/task-tree-elem';
import { AddEvent } from 'src/app/common/utils/add-event';
import { DateEvent } from 'src/app/common/utils/date-event';
import { CollabTaskService } from 'src/app/task/collab-task.service';
import { ProjectService } from '../../project/project.service';
import { TaskService } from 'src/app/task/task.service';
import { TreeService } from 'src/app/utils/tree.service';

export interface TaskForm {
  title: FormControl<string>;
  description: FormControl<string>;

}

@Component({
  selector: 'app-task-form',
  templateUrl: './task-form.component.html',
  styleUrls: ['./task-form.component.css']
})
export class TaskFormComponent implements OnInit {
  @Output() closeEvent = new EventEmitter<boolean>();
  taskForm: FormGroup<TaskForm> | undefined;
  showSelectProjectMenu: boolean = false;
  projects: ProjectTreeElem[] = [];
  showSelectDateMenu: boolean = false;
  taskDate: Date | undefined;
  taskTimeboxed: boolean = false;

  @Input() project: ProjectTreeElem | undefined;
  @Input() collab: boolean = false;

  task: TaskTreeElem | undefined;
  after: boolean = false;
  before: boolean = false;
  asChild: boolean = false;

  @Input() data: AddEvent<TaskTreeElem> | undefined;
  @Input("date") date: Date | undefined;
  @Input("timeboxed") timeboxed: boolean = false;

  constructor(public tree: TreeService, private service: ProjectService, 
    private fb: FormBuilder, private taskService: TaskService, private collabTaskService: CollabTaskService) {  }

  ngOnInit(): void {
    if(this.data) {
      this.task = this.data.object;
      this.after = this.data.after;
      this.before = this.data.before;
      this.asChild =  this.data.asChild;
    }

    if(this.date) {
      this.taskDate = this.date;
    }
    this.taskTimeboxed = this.timeboxed;

    this.taskForm = this.fb.nonNullable.group({
      title: [this.task && !this.after && !this.before && !this.asChild ? this.task.title : '', Validators.required],
      description: [this.task && !this.after && !this.before && !this.asChild  ? this.task.description : '', []]
    });
    
    if(this.task && !this.after && !this.before && !this.asChild ) {
		  this.taskDate = this.task.due ? this.task.due : undefined;
      this.priority = this.task.priority;
      this.labels = this.task.labels;
      if(!this.project) {
        this.project = this.task.project ? (this.collab ? this.tree.getCollabProjectById(this.task.project.id) : this.tree.getProjectById(this.task.project.id)) : undefined;
      }
	  }
    
    if(this.task && this.asChild) {
      if(!this.project) {
        this.project = this.task.project ? (this.collab ? this.tree.getCollabProjectById(this.task.project.id) : this.tree.getProjectById(this.task.project.id)) : undefined;
      }
    }
  }

  openSelectProjectMenu() {
    if(this.collab) {
      return;
    }
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

  closeSelectDateMenu(date: DateEvent) {
    this.taskDate = date.date;
    this.taskTimeboxed = date.timeboxed;
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
      if(this.collab) {
        this.addCollabTask();
      } else {
        this.addTask();
      }
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
        timeboxed: this.taskTimeboxed,
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

  addCollabTask() {
    if(this.taskForm && this.taskForm.valid && this.project) {
      this.service.addCollabTask({
        title: this.taskForm.controls.title.value,
        description: this.taskForm.controls.description.value,
        projectOrder: 0,
        parentId: null,
        projectId: this.project ? this.project.id : null,
        priority: this.priority,
        due: this.taskDate ? this.taskDate : null,
        timeboxed: this.taskTimeboxed,
        completedAt: null,
        labelIds: []
      }, this.project.id).subscribe(
        (response: Task, projectId: number | undefined = this.project?.id) => {
          this.tree.addNewCollabTask(response, projectId);
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
    let service = this.collab ? this.collabTaskService : this.taskService;
    if(this.task && this.taskForm && this.taskForm.valid) {
      let lbls = this.labels.map((a) => a.id);
      service.updateTask({
        title: this.taskForm.controls.title.value,
        description: this.taskForm.controls.description.value,
        projectOrder: this.task.order,
        parentId: this.task.parent ? this.task.parent.id : null,
        projectId: this.project ? this.project.id : null,
        priority: this.priority,
        due: this.taskDate ? this.taskDate : null,
        timeboxed: this.taskTimeboxed,
        completedAt: null,
        labelIds: lbls
      }, this.task.id).subscribe(
        (response: Task, projectId: number | undefined = this.project?.id, labels: number[] = lbls) => {
          if(this.collab) {
            this.tree.updateCollabTask(response, projectId, labels);

          } else {
            this.tree.updateTask(response, projectId, labels);
          }
        },
        (error: HttpErrorResponse) => {
         
        }
      );
    }
  }

  addTaskAfter() {
    let service = this.collab ? this.collabTaskService : this.taskService;
    if(this.task && this.taskForm && this.taskForm.valid) {
      let indentAfter = this.task.indent;
      let idAfter = this.task.id;
      let lbls = this.labels.map((a) => a.id);
      service.addTaskAfter({
        title: this.taskForm.controls.title.value,
        description: this.taskForm.controls.description.value,
        projectOrder: this.task.order,
        parentId: this.task.id,
        projectId: this.project ? this.project.id : null,
        priority: this.priority,
        due: this.taskDate ? this.taskDate : null,
        timeboxed: this.taskTimeboxed,
        completedAt: null,
        labelIds: lbls
      }, this.task.id).subscribe(
        (response: Task, indent: number = indentAfter, taskId: number = idAfter, project: ProjectTreeElem | undefined = this.project, labels: number[] = lbls) => {
          if(this.collab) {
            this.tree.addNewCollabTaskAfter(response, indent, taskId, project);

          } else {
            this.tree.addNewTaskAfter(response, indent, taskId, project, labels);
          }
        },
        (error: HttpErrorResponse) => {
         
        }
      );
    }
  }

  addTaskBefore() {
    let service = this.collab ? this.collabTaskService : this.taskService;
    if(this.task && this.taskForm && this.taskForm.valid) {
      let indentBefore = this.task.indent;
      let idBfore = this.task.id;
      let lbls = this.labels.map((a) => a.id);
      service.addTaskBefore({
        title: this.taskForm.controls.title.value,
        description: this.taskForm.controls.description.value,
        projectOrder: this.task.order,
        parentId: this.task.id,
        projectId: this.project ? this.project.id : null,
        priority: this.priority,
        due: this.taskDate ? this.taskDate : null,
        timeboxed: this.taskTimeboxed,
        completedAt: null,
        labelIds: lbls
      }, this.task.id).subscribe(
        (response: Task, indent: number = indentBefore, taskId: number = idBfore, project: ProjectTreeElem | undefined = this.project, labels: number[] = lbls) => {
          if(this.collab) {
            this.tree.addNewCollabTaskBefore(response, indent, taskId, project);

          } else {
            this.tree.addNewTaskBefore(response, indent, taskId, project, labels);
          }
        },
        (error: HttpErrorResponse) => {
         
        }
      );
    }
  }

  addTaskAsChild() {
    let service = this.collab ? this.collabTaskService : this.taskService;
    if(this.task && this.taskForm && this.taskForm.valid) {
      let indentChild = this.task.indent+1;
      let idParent = this.task.id;
      let lbls = this.labels.map((a) => a.id);
      service.addTaskAsChild({
        title: this.taskForm.controls.title.value,
        description: this.taskForm.controls.description.value,
        projectOrder: this.task.order,
        parentId: this.task.id,
        projectId: this.project ? this.project.id : null,
        priority: this.priority,
        due: this.taskDate ? this.taskDate : null,
        timeboxed: this.taskTimeboxed,
        completedAt: null,
        labelIds: lbls
      }, this.task.id).subscribe(
        (response: Task, indent: number = indentChild, taskId: number = idParent, project: ProjectTreeElem | undefined = this.project, labels: number[] = lbls) => {
          if(this.collab) {
            this.tree.addNewCollabTaskAsChild(response, indent, taskId, project);

          } else {
            this.tree.addNewTaskAsChild(response, indent, taskId, project, labels);
          }
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

  get subModalOpened(): boolean {
    return this.showSelectDateMenu || this.showSelectLabelsMenu || this.showSelectPriorityMenu || this.showSelectProjectMenu;
  }

  @HostListener("window:keydown.escape", ["$event"])
  handleKeyboardEscapeEvent() {
    if(!this.subModalOpened) {
      this.closeForm();
    }
  }

  @HostListener("window:keydown.enter", ["$event"])
  handleKeyboardEnterEvent() {
    if(!this.subModalOpened && this.taskForm?.valid) {
      this.save();
    }
  }
}
