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
  dateSelectForm: FormGroup | undefined;

  today: Date;
  tomorrow: Date;
  weekend: Date;
  nextWeek: Date;

  constructor(public tree: TreeService, private service: ProjectService, 
    private fb: FormBuilder, private taskService: TaskService) { 
      this.today = new Date();
      let dayOfTheMonth = this.today.getDate();
      let dayOfTheWeek = this.today.getDay();
      this.tomorrow = new Date();
      this.tomorrow.setDate(dayOfTheMonth + 1);
      this.weekend = new Date();
      this.weekend.setDate(dayOfTheMonth - dayOfTheWeek + 6);
      this.nextWeek = new Date();
      this.nextWeek.setDate(this.weekend.getDate() + 2);
    }

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

  formatDate(date: Date): String {
    return date.toISOString().split("T")[0];
  }

  dateWithinWeek(date: Date): boolean {
    let dateToCompare: Date = new Date(this.today);
    dateToCompare.setDate(dateToCompare.getDate() + 9);
    dateToCompare.setHours(0);
    dateToCompare.setMinutes(0);
    dateToCompare.setSeconds(0);
    dateToCompare.setMilliseconds(0);
    return date < dateToCompare;
  }

  openSelectDateMenu() {
    this.dateSelectForm = this.fb.group(
      {
        date: [this.taskDate ? this.formatDate(this.taskDate) : '', Validators.required]
      }
      );
    this.showSelectDateMenu = true;
  }

  closeSelectDateMenu() {
    this.showSelectDateMenu = false;
  }

  closeForm() {
    this.closeEvent.emit(true);
  }

  chooseDate(date: Date | undefined) {
    this.closeSelectDateMenu();
    this.taskDate = date;
  } 

  selectDateFromForm() {
    if(this.dateSelectForm) {
      this.taskDate = new Date(this.dateSelectForm.controls.date.value);
    }
    this.closeSelectDateMenu();
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
