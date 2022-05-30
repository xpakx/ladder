import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, Input, OnInit, Output, Renderer2, ViewChild, EventEmitter } from '@angular/core';
import { Router } from '@angular/router';
import { DndDropEvent } from 'ngx-drag-drop';
import { LabelDetails } from 'src/app/label/dto/label-details';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { Task } from 'src/app/task/dto/task';
import { TaskTreeElem } from 'src/app/task/dto/task-tree-elem';
import { AddEvent } from 'src/app/common/utils/add-event';
import { DateEvent } from 'src/app/common/utils/date-event';
import { CollabTaskService } from 'src/app/task/collab-task.service';
import { DeleteService } from 'src/app/utils/delete.service';
import { TaskTreeService } from 'src/app/task/task-tree.service';
import { TaskService } from 'src/app/task/task.service';
import { TreeService } from 'src/app/utils/tree.service';
import { DraggableComponent } from 'src/app/common/draggable-component';

@Component({
  selector: 'app-task-daily-list',
  templateUrl: './task-daily-list.component.html',
  styleUrls: ['./task-daily-list.component.css']
})
export class TaskDailyListComponent  extends DraggableComponent<TaskTreeElem, Task, TaskService, TaskTreeService>
implements OnInit {
  @Input("tasks") tasks: TaskTreeElem[] = [];
  @Input("overdue") overdue: boolean = false;
  @Input("collab") collab: boolean = false;
  @Input("dnd") dnd: boolean = false;
  @Input("multi") multipanel: Date | undefined;
  @Output() closeAddForm = new EventEmitter<boolean>();
  @Output() dragEnd = new EventEmitter<boolean>();
  @Output() dragStart = new EventEmitter<boolean>();

  constructor(public tree: TreeService, private router: Router,
    private taskService: TaskService, private taskTreeService: TaskTreeService,
    private renderer: Renderer2, private deleteService: DeleteService, 
    private collabTaskService: CollabTaskService) { 
      super(taskTreeService, taskService);
    }

  ngOnInit(): void {
  }

  showAddTaskForm: boolean = false;
  taskData: AddEvent<TaskTreeElem> = new AddEvent<TaskTreeElem>();

  getProjectColor(id: number): string {
    let project = this.collab ? this.tree.getCollabProjectById(id) : this.tree.getProjectById(id);
    return project ? project.color : ""
  }

  onDrop(event: DndDropEvent, target: TaskTreeElem) {
    let id = Number(event.data);
    this.taskService.moveAfterDaily({id: target.id}, id).subscribe(
        (response: Task, afterId: number = target.id) => {
        this.taskTreeService.moveAfterDaily(response, afterId);
      },
      (error: HttpErrorResponse) => {
      
      }
    );

    this.dragEnd.emit(true);
  }

  onDropFirst(event: DndDropEvent) {
    let id = Number(event.data);
    if(!this.multipanel) {
      if(!this.dnd) {
        this.moveAsFirst(id);
      } else {
        this.moveAsFirstWithDate(id, new Date());
      }
    } else {
      this.moveAsFirstWithDate(id, this.multipanel);
    }

    this.dragEnd.emit(true);
  }

  private moveAsFirst(id: number) {
    this.taskService.moveAsFirstDaily(id).subscribe(
      (response: Task) => {
        this.taskTreeService.moveAsFirstDaily(response);
      },
      (error: HttpErrorResponse) => {
      }
    );
  }

  private moveAsFirstWithDate(id: number, date: Date) {
    this.taskService.moveAsFirstWithDate(id, { date: date, timeboxed: false }).subscribe(
      (response: Task) => {
        this.taskTreeService.moveAsFirstDaily(response);
      },
      (error: HttpErrorResponse) => {
      }
    );
  }

  onDragStart(id: number) {
    this.draggedId = id;
    this.dragStart.emit(true);
  }

  onDragEnd() {
    this.draggedId = undefined;
    this.dragEnd.emit(true);
  }

  toProject() {
    if(this.contextTaskMenu && this.contextTaskMenu.project) {
      this.router.navigate([(this.collab ? '/collab/': '/project/')+this.contextTaskMenu.project.id]);
    } else if(this.contextTaskMenu) {
      this.router.navigate(['/inbox']);
    }
  }
  
  closeAddTaskForm() {
    this.closeAddForm.emit(true);
  }

  openEditTaskForm(task: TaskTreeElem) {
    this.closeAddTaskForm();
    this.taskData = new AddEvent<TaskTreeElem>(task);
  }

  closeEditTaskForm() {
    this.taskData = new AddEvent<TaskTreeElem>();
  }

  openEditTaskFromContextMenu() {
    if(this.contextTaskMenu) {
      this.closeAddTaskForm();
      this.taskData = new AddEvent<TaskTreeElem>(this.contextTaskMenu);
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
    let task = this.collab ? this.tree.getCollabTaskById(id) : this.tree.getTaskById(id);
    let service = this.collab ? this.collabTaskService : this.taskService;
    if(task) {
    service.completeTask(id, {flag: !task.completed}).subscribe(
        (response: Task) => {
          if(this.collab) {
            this.tree.changeCollabTaskCompletion(response);

          } else {
            this.tree.changeTaskCompletion(response);
          }
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
    this.contextTaskMenu = this.collab ? this.tree.getCollabTaskById(taskId) : this.tree.getTaskById(taskId);
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
      if(this.collab) {
        this.deleteService.openModalForCollabTask(this.contextTaskMenu);
      } else {
        this.deleteService.openModalForTask(this.contextTaskMenu);
      }
    }
    this.closeContextTaskMenu();
  }

  showSelectDateModal: boolean = false;
  dateForDateModal: Date | undefined;
  taskIdForDateModal: number | undefined;

  closeSelectDateModal(date: DateEvent) {
    this.showSelectDateModal = false;
    let service = this.collab ? this.collabTaskService : this.taskService;
    if(this.taskIdForDateModal) {
      service.updateTaskDueDate({date: date.date, timeboxed: date.timeboxed}, this.taskIdForDateModal).subscribe(
          (response: Task) => {
            if(this.collab) {
              this.tree.updateCollabTaskDate(response);
            } else {
              this.tree.updateTaskDate(response);
            }
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
    this.dateForDateModal = undefined;
    this.taskIdForDateModal = undefined;
  }

  cancelDateSelection() {
    this.showSelectDateModal = false;
    this.dateForDateModal = undefined;
    this.taskIdForDateModal = undefined;
  }

  openSelectDateModal(task: TaskTreeElem) {
    this.taskIdForDateModal = task.id;
    this.dateForDateModal = task.due ? task.due : undefined;
    this.showSelectDateModal = true;
  }

  openSelectDateModalFormContextMenu() {
    if(this.contextTaskMenu) {
      this.openSelectDateModal(this.contextTaskMenu);
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
  }

  cancelProjectSelection() {
    this.showSelectProjectModal = false;
    this.projectForProjectModal = undefined;
    this.taskIdForProjectModal = undefined;
  }

  openSelectProjectModal(task: TaskTreeElem) {
    this.taskIdForProjectModal = task.id;
    let project = task.project ? this.tree.getProjectById(task.project.id) : undefined;
    this.projectForProjectModal = project;
    this.showSelectProjectModal = true;
  }

  openSelectProjectModalFormContextMenu() {
    if(this.contextTaskMenu) {
      this.openSelectProjectModal(this.contextTaskMenu);
    }
    this.closeContextTaskMenu();
  }

  showSelectPriorityModal: boolean = false;
  priorityForPriorityModal: number = 0;
  taskIdForPriorityModal: number | undefined;

  closeSelectPriorityModal(priority: number) {
    this.showSelectPriorityModal = false;
    let service = this.collab ? this.collabTaskService : this.taskService;
    if(this.taskIdForPriorityModal) {
      service.updateTaskPriority({priority: priority}, this.taskIdForPriorityModal).subscribe(
          (response: Task) => {
            if(this.collab) {
              this.tree.updateCollabTaskPriority(response);
            } else {
              this.tree.updateTaskPriority(response);
            }
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
    this.priorityForPriorityModal = 0;
    this.taskIdForPriorityModal = undefined;
  }

  cancelPrioritySelection() {
    this.showSelectPriorityModal = false;
    this.priorityForPriorityModal = 0;
    this.taskIdForPriorityModal = undefined;
  }

  openSelectPriorityModal(task: TaskTreeElem) {
    this.taskIdForPriorityModal = task.id;
    this.priorityForPriorityModal = task.priority;
    this.showSelectPriorityModal = true;
  }

  openSelectPriorityModalFormContextMenu() {
    if(this.contextTaskMenu) {
      this.openSelectPriorityModal(this.contextTaskMenu);
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
