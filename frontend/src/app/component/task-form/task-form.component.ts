import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit, Output, EventEmitter, Renderer2, ViewChild, ElementRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { Task } from 'src/app/entity/task';
import { TaskDetails } from 'src/app/entity/task-details';
import { ProjectService } from 'src/app/service/project.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-task-form',
  templateUrl: './task-form.component.html',
  styleUrls: ['./task-form.component.css']
})
export class TaskFormComponent implements OnInit {
  @Input() task: TaskDetails | undefined;
  @Input() project: ProjectTreeElem | undefined;
  @Output() closeEvent = new EventEmitter<boolean>();
  taskForm: FormGroup | undefined;
  showSelectProjectMenu: boolean = false;
  projectContextMenuX: number = 0;
  projectContextMenuY: number = 0;
  @ViewChild('projectSelectMenu', {read: ElementRef}) projectContextMenuElem!: ElementRef;

  constructor(public tree: TreeService, private service: ProjectService, 
    private fb: FormBuilder, private renderer: Renderer2) { }

  ngOnInit(): void {
    this.taskForm = this.fb.group({
      title: [this.task ? this.task.title : '', Validators.required],
      description: [this.task ? this.task.description : '', []]
    });
  }

  ngAfterViewInit() {
    this.renderer.listen('window', 'click',(e:Event)=>{
      if(this.showSelectProjectMenu &&
        e.target !== this.projectContextMenuElem.nativeElement){
          this.showSelectProjectMenu = false;
      }
    });
  }

  openSelectProjectMenu(event: MouseEvent) {
    this.showSelectProjectMenu = true;
    this.projectContextMenuX = event.clientX;
    this.projectContextMenuY = event.clientY;
    event.stopPropagation();
  }

  closeForm() {
    this.closeEvent.emit(true);
  }

  chooseProject(project: ProjectTreeElem | undefined) {
    this.project = project;
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
        due: null,
        completedAt: null
      }, this.project ? this.project.id : undefined).subscribe(
        (response: Task, projectId: number | undefined = this.project?.id) => {
          this.tree.addNewTask(response, projectId);
        },
        (error: HttpErrorResponse) => {
         
        }
      );
      this.closeEvent.emit(true);
    }
  }
}
