import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit, Output, EventEmitter, Renderer2, ViewChild, ElementRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { Task } from 'src/app/entity/task';
import { TaskDetails } from 'src/app/entity/task-details';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { ProjectService } from 'src/app/service/project.service';
import { TaskService } from 'src/app/service/task.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-task-form',
  templateUrl: './task-form.component.html',
  styleUrls: ['./task-form.component.css']
})
export class TaskFormComponent implements OnInit {
  @Input() task: TaskTreeElem | undefined;
  @Input() project: ProjectTreeElem | undefined;
  @Output() closeEvent = new EventEmitter<boolean>();
  taskForm: FormGroup | undefined;
  showSelectProjectMenu: boolean = false;
  projects: ProjectTreeElem[] = [];
  showSelectDateMenu: boolean = false;
  taskDate: Date | undefined;

  projectSelectForm: FormGroup | undefined;


  @Input() after: boolean = false;
  @Input() before: boolean = false;


  constructor(public tree: TreeService, private service: ProjectService, 
    private fb: FormBuilder, private taskService: TaskService) {  }

  ngOnInit(): void {
    this.taskForm = this.fb.group({
      title: [this.task ? this.task.title : '', Validators.required],
      description: [this.task ? this.task.description : '', []]
    });
    
    if(this.task) {
		  this.taskDate = this.task.due ? this.task.due : undefined;
	  }
  }

  getProjects(text: string = "") {
    this.projects = this.tree.filterProjects(text);
  }

  openSelectProjectMenu() {
    this.projectSelectForm = this.fb.group({text: ''});
    this.projectSelectForm.valueChanges.subscribe(data => {
      this.getProjects(data.text);
    });
    this.getProjects();
    this.showSelectProjectMenu = true;
  }

  closeSelectProjectMenu() {
    this.showSelectProjectMenu = false;
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

  chooseProject(project: ProjectTreeElem | undefined) {
    this.closeSelectProjectMenu();
    this.project = project;
  }

  save() {
    if(this.task) {
      this.updateTask();
    } else {
      this.addTask();
    }

    this.closeEvent.emit(true);
  }
  
  addTask() {
    if(this.taskForm && this.taskForm.valid) {
      this.service.addTask({
        title: this.taskForm.controls.title.value,
        description: this.taskForm.controls.description.value,
        projectOrder: 0,
        parentId: null,
        projectId: this.project ? this.project.id : null,
        priority: 0,
        due: this.taskDate ? this.taskDate : null,
        completedAt: null
      }, this.project ? this.project.id : undefined).subscribe(
        (response: Task, projectId: number | undefined = this.project?.id) => {
          this.tree.addNewTask(response, projectId);
        },
        (error: HttpErrorResponse) => {
         
        }
      );
    }
  }

  updateTask() {
    if(this.task && this.taskForm && this.taskForm.valid) {

      this.taskService.updateTask({
        title: this.taskForm.controls.title.value,
        description: this.taskForm.controls.description.value,
        projectOrder: this.task.order,
        parentId: this.task.parent ? this.task.parent.id : null,
        projectId: this.project ? this.project.id : null,
        priority: 0,
        due: this.taskDate ? this.taskDate : null,
        completedAt: null
      }, this.task.id).subscribe(
        (response: Task, projectId: number | undefined = this.project?.id) => {
          this.tree.updateTask(response, projectId);
        },
        (error: HttpErrorResponse) => {
         
        }
      );
    }
  }
}
