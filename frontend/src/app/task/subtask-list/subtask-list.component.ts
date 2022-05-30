import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, Input, OnInit, Output, Renderer2, ViewChild, EventEmitter } from '@angular/core';
import { Router } from '@angular/router';
import { LabelDetails } from 'src/app/label/dto/label-details';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { Task } from 'src/app/task/dto/task';
import { TaskTreeElem } from 'src/app/task/dto/task-tree-elem';
import { AddEvent } from 'src/app/common/utils/add-event';
import { DateEvent } from 'src/app/common/utils/date-event';
import { DeleteService } from 'src/app/utils/delete.service';
import { TaskTreeService } from 'src/app/task/task-tree.service';
import { TaskService } from 'src/app/task/task.service';
import { TreeService } from 'src/app/utils/tree.service';
import { MultilevelTaskComponent } from '../multilevel-task-component';

@Component({
  selector: 'app-subtask-list',
  templateUrl: './subtask-list.component.html',
  styleUrls: ['./subtask-list.component.css']
})
export class SubtaskListComponent extends MultilevelTaskComponent<TaskTreeService> 
 implements OnInit {

  public invalid: boolean = false;
  public message: string = '';
  @Input("task") parent?: TaskTreeElem;
  @Input("taskList") taskList: TaskTreeElem[] = [];
  @Output("submodal") submodalEvent = new EventEmitter<boolean>();

  constructor(private router: Router, public tree: TreeService, 
    private taskService: TaskService, private taskTreeService: TaskTreeService,
    private renderer: Renderer2, private deleteService: DeleteService) {
    super(taskTreeService, taskService);
   }

  ngOnInit(): void {
    if(!this.tree.isLoaded()) {
      this.router.navigate(["load"]);
    }

    if(this.parent && this.parent.project) {
      this.project = this.tree.getProjectById(this.parent.project.id);
    }
  }

  get tasks(): TaskTreeElem[] {
    if(this.taskList.length > 0) {
      return this.taskList;
    }
    return this.parent ? [...this.taskTreeService.getByParentId(this.parent.id)] : [];
  }

  get list(): boolean {
    return this.taskList.length > 0;
  }

  get newTaskData(): AddEvent<TaskTreeElem> | undefined {
    return new AddEvent<TaskTreeElem>(this.parent, false, false, true);
  }

  protected getElems(): TaskTreeElem[] {
    return this.tasks;
  }

  showAddTaskForm: boolean = false;
  taskData: AddEvent<TaskTreeElem> = new AddEvent<TaskTreeElem>();

  getProjectColor(id: number): string {
    let project = this.tree.getProjectById(id)
    return project ? project.color : ""
  }

  toProject() {
    if(this.contextTaskMenu && this.contextTaskMenu.project) {
      this.router.navigate(['/project/'+this.contextTaskMenu.project.id]);
    } else if(this.contextTaskMenu) {
      this.router.navigate(['/inbox']);
    }
  }
  // Task form
  openAddTaskForm() {
    this.closeEditTaskForm();
    this.showAddTaskForm = true;
    this.taskData = new AddEvent<TaskTreeElem>();
    this.submodalEvent.emit(true);
  }

  closeAddTaskForm() {
    this.showAddTaskForm = false;
    this.submodalEvent.emit(false);
  }

  openEditTaskForm(task: TaskTreeElem) {
    this.closeAddTaskForm();
    this.taskData = new AddEvent<TaskTreeElem>(task);
    this.submodalEvent.emit(true);
  }

  closeEditTaskForm() {
    this.taskData = new AddEvent<TaskTreeElem>();
    this.submodalEvent.emit(false);
  }

  openEditTaskFromContextMenu() {
    if(this.contextTaskMenu) {
      this.closeAddTaskForm();
      this.taskData = new AddEvent<TaskTreeElem>(this.contextTaskMenu);
      this.submodalEvent.emit(true);
    }
    this.closeContextTaskMenu();
  }

  shouldEditTaskById(taskId: number): boolean {
    return this.taskObjectContains(taskId) && this.taskData.isInEditMode();
  }

  taskObjectContains(taskId: number) {
  return taskId == this.taskData.object?.id;
  }

  completeTask(id: number) {
    let task = this.tree.getTaskById(id);
    if(task) {
    this.taskService.completeTask(id, {flag: !task.completed}).subscribe(
        (response: Task) => {
        this.tree.changeTaskCompletion(response);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
    }
  }

  contextTaskMenu: TaskTreeElem | undefined;
  showContextTaskMenu: boolean = false;
  contextTaskMenuJustOpened: boolean = false;
  taskContextMenuX: number = 0;
  taskContextMenuY: number = 0;
  @ViewChild('taskContext', {read: ElementRef}) taskContextMenuElem!: ElementRef;


  ngAfterViewInit() {
    this.renderer.listen('window', 'click',(e:Event)=>{
      if(this.showContextTaskMenu && 
        !this.taskContextMenuElem.nativeElement.contains(e.target)){
        if(this.contextTaskMenuJustOpened) {
          this.contextTaskMenuJustOpened = false
        } else {
          this.showContextTaskMenu = false;
        }
      }
    })
  }

  openContextTaskMenu(event: MouseEvent, taskId: number) {
    this.contextTaskMenu = this.tree.getTaskById(taskId);
    this.showContextTaskMenu = true;
    this.contextTaskMenuJustOpened = true;
    this.taskContextMenuX = event.clientX-250;
    this.taskContextMenuY = event.clientY;
  }

  closeContextTaskMenu() {
    this.contextTaskMenu = undefined;
    this.showContextTaskMenu = false;
  }

  getNumOfCompletedTasksByParent(parentId: number): number {
    return this.tree.getNumOfCompletedTasksByParent(parentId);
  }

  getNumOfTasksByParent(parentId: number): number {
    return this.tree.getNumOfTasksByParent(parentId);
  }

  askForDelete() {
    if(this.contextTaskMenu) {
      this.deleteService.openModalForTask(this.contextTaskMenu);
    }
    this.closeContextTaskMenu();
  }

  showSelectDateModal: boolean = false;
  dateForDateModal: Date | undefined;
  taskIdForDateModal: number | undefined;

  closeSelectDateModal(date: DateEvent) {
    this.showSelectDateModal = false;
    if(this.taskIdForDateModal) {
      this.taskService.updateTaskDueDate({date: date.date, timeboxed: date.timeboxed}, this.taskIdForDateModal).subscribe(
          (response: Task) => {
          this.tree.updateTaskDate(response);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
    this.dateForDateModal = undefined;
    this.taskIdForDateModal = undefined;
    this.submodalEvent.emit(false);
  }

  cancelDateSelection() {
    this.showSelectDateModal = false;
    this.dateForDateModal = undefined;
    this.taskIdForProjectModal = undefined;
    this.taskIdForDateModal = undefined;
    this.submodalEvent.emit(false);
  }

  openSelectDateModal(task: TaskTreeElem) {
    this.taskIdForDateModal = task.id;
    this.dateForDateModal = task.due ? task.due : undefined;
    this.showSelectDateModal = true;
    this.submodalEvent.emit(true);
  }

  openSelectDateModalFormContextMenu() {
    if(this.contextTaskMenu) {
      this.openSelectDateModal(this.contextTaskMenu);
      this.submodalEvent.emit(true);
    }
    this.closeContextTaskMenu();
  }

  showSelectProjectModal: boolean = false;
  projectForProjectModal: ProjectTreeElem | undefined;
  taskIdForProjectModal: number | undefined;

  closeSelectProjectModal(project: ProjectTreeElem | undefined) {
    this.showSelectProjectModal = false;
    if(this.taskIdForProjectModal) {
      this.taskService.updateTaskProject({id: project? project.id : undefined}, this.taskIdForProjectModal).subscribe(
          (response: Task, proj: ProjectTreeElem | undefined = project) => {
          this.tree.moveTaskToProject(response, proj);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
    this.projectForProjectModal = undefined;
    this.taskIdForProjectModal = undefined;
    this.submodalEvent.emit(false);
  }

  cancelProjectSelection() {
    this.showSelectProjectModal = false;
    this.projectForProjectModal = undefined;
    this.taskIdForProjectModal = undefined;
    this.submodalEvent.emit(false);
  }

  openSelectProjectModal(task: TaskTreeElem) {
    this.taskIdForProjectModal = task.id;
    let project = task.project ? this.tree.getProjectById(task.project.id) : undefined;
    this.projectForProjectModal = project;
    this.showSelectProjectModal = true;
    this.submodalEvent.emit(true);
  }

  openSelectProjectModalFormContextMenu() {
    if(this.contextTaskMenu) {
      this.openSelectProjectModal(this.contextTaskMenu);
      this.submodalEvent.emit(true);
    }
    this.closeContextTaskMenu();
  }

  showSelectPriorityModal: boolean = false;
  priorityForPriorityModal: number = 0;
  taskIdForPriorityModal: number | undefined;

  closeSelectPriorityModal(priority: number) {
    this.showSelectPriorityModal = false;
    if(this.taskIdForPriorityModal) {
      this.taskService.updateTaskPriority({priority: priority}, this.taskIdForPriorityModal).subscribe(
          (response: Task) => {
          this.tree.updateTaskPriority(response);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
    this.priorityForPriorityModal = 0;
    this.taskIdForPriorityModal = undefined;
    this.submodalEvent.emit(false);
  }

  cancelPrioritySelection() {
    this.showSelectPriorityModal = false;
    this.priorityForPriorityModal = 0;
    this.taskIdForPriorityModal = undefined;
    this.submodalEvent.emit(false);
  }

  openSelectPriorityModal(task: TaskTreeElem) {
    this.taskIdForPriorityModal = task.id;
    this.priorityForPriorityModal = task.priority;
    this.showSelectPriorityModal = true;
    this.submodalEvent.emit(true);
  }

  openSelectPriorityModalFormContextMenu() {
    if(this.contextTaskMenu) {
      this.openSelectPriorityModal(this.contextTaskMenu);
      this.submodalEvent.emit(true);
    }
    this.closeContextTaskMenu();
  }

  getTaskLabels(task: TaskTreeElem): LabelDetails[] {
    let labels: LabelDetails[] = [];
    for(let label of task.labels) {
      let labelFromTree = this.tree.getLabelById(label.id);
      if(labelFromTree) {
        labels.push(labelFromTree);
      }
    }
    return labels;
  }
}
