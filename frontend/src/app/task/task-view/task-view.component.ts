import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, HostListener, Input, OnInit, Output } from '@angular/core';
import { LabelDetails } from 'src/app/label/dto/label-details';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { Task } from 'src/app/task/dto/task';
import { TaskTreeElem } from 'src/app/task/dto/task-tree-elem';
import { AddEvent } from 'src/app/common/utils/add-event';
import { DateEvent } from 'src/app/common/utils/date-event';
import { CollabTaskTreeService } from 'src/app/task/collab-task-tree.service';
import { CollabTaskService } from 'src/app/task/collab-task.service';
import { DeleteService } from 'src/app/utils/delete.service';
import { TaskTreeService } from 'src/app/task/task-tree.service';
import { TaskService } from 'src/app/task/task.service';
import { TreeService } from 'src/app/utils/tree.service';

@Component({
  selector: 'app-task-view',
  templateUrl: './task-view.component.html',
  styleUrls: ['./task-view.component.css']
})
export class TaskViewComponent implements OnInit {
  @Input("task") parent?: TaskTreeElem;
  @Input("collab") collab: boolean = false;
  @Output() closeEvent = new EventEmitter<boolean>();
  edit: boolean = false;
  parentData!: AddEvent<TaskTreeElem>;
  menuChoice: number = 0;
  taskListSubmodalOpened: boolean = false;
  collabTaskListSubmodalOpened: boolean = false;
  commentListSubmodalOpened: boolean = false;

  constructor(private taskTree: TaskTreeService, private taskService: TaskService, 
    private collabService: CollabTaskService, private collabTree: CollabTaskTreeService,
    private tree: TreeService, private deleteService: DeleteService) { }

  ngOnInit(): void {
    if(parent) {
      this.parentData = new AddEvent<TaskTreeElem>(this.parent);
    }
  }

  closeModal() {
    this.closeEvent.emit(true);
  }

  closeEditTaskForm() {
    this.edit = false;
  }

  complete() {
    if(this.parent) {
      let service = this.collab ? this.collabService : this.taskService;
      let tree = this.collab ? this.collabTree : this.taskTree;
      service.completeTask(this.parent.id, {flag: !this.parent.completed}).subscribe(
        (response: Task) => {
        tree.changeTaskCompletion(response);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
    }
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

  showSelectDateModal:boolean = false;

  openSelectDateModal() {
    this.showSelectDateModal = true;
  }

  closeSelectDateModal(date: DateEvent) {
    this.showSelectDateModal = false;
    if(this.parent) {
      let service = this.collab ? this.collabService : this.taskService;
      let tree = this.collab ? this.collabTree : this.taskTree;
      service.updateTaskDueDate({date: date.date, timeboxed: date.timeboxed}, this.parent.id).subscribe(
          (response: Task) => {
          tree.updateTaskDate(response);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
    this.showSelectDateModal = false;
  }
  
  cancelDateSelection() {
    this.showSelectDateModal = false;
  }

  showSelectProjectModal: boolean = false;
  projectForProjectModal: ProjectTreeElem | undefined;

  closeSelectProjectModal(project: ProjectTreeElem | undefined) {
    this.showSelectProjectModal = false;
    if(this.parent) {
      this.taskService.updateTaskProject({id: project? project.id : undefined}, this.parent.id).subscribe(
          (response: Task, proj: ProjectTreeElem | undefined = project) => {
          this.taskTree.moveTaskToProject(response, proj);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }

  cancelProjectSelection() {
    this.showSelectProjectModal = false;
  }

  openSelectProjectModal() {
    if(this.parent) {
      let project = this.parent.project ? this.tree.getProjectById(this.parent.project.id) : undefined;
      this.projectForProjectModal = project;
      this.showSelectProjectModal = true;
    }
  }

  askForDelete() {
    if(this.parent) {
      if(this.collab) {
        this.deleteService.openModalForCollabTask(this.parent);
      } else {
        this.deleteService.openModalForTask(this.parent);
      }
    }
  }

  showSelectPriorityModal: boolean = false;
  priorityForPriorityModal: number = 0;

  closeSelectPriorityModal(priority: number) {
    this.showSelectPriorityModal = false;
    if(this.parent) {
      let service = this.collab ? this.collabService : this.taskService;
      let tree = this.collab ? this.collabTree : this.taskTree;
      service.updateTaskPriority({priority: priority}, this.parent.id).subscribe(
          (response: Task) => {
          tree.updateTaskPriority(response);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }

  cancelPrioritySelection() {
    this.showSelectPriorityModal = false;
  }

  openSelectPriorityModal() {
    if(this.parent) {
      this.priorityForPriorityModal = this.parent.priority;
      this.showSelectPriorityModal = true;
    }
  }

  chooseTab(i: number) {
    this.menuChoice = i;
  }

  labelsForModal: LabelDetails[] = [];
  showSelectLabelsMenu: boolean = false;

  openSelectLabelsMenu() {
    if(this.parent) {
      this.labelsForModal = [...this.parent.labels];
    }
    this.showSelectLabelsMenu = true;
  }

  closeSelectLabelsMenu() {
    this.showSelectLabelsMenu = false;
  }

  chooseLabel(labels: LabelDetails[]) {
    this.closeSelectLabelsMenu();
    if(this.parent) {
      this.taskService.updateTaskLabels({
        ids: labels.map((a) => a.id)
      }, this.parent.id).subscribe(
          (response: Task, labelsToUpdate = labels) => {
          this.tree.updateTaskLabels(response, labelsToUpdate);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }

  get subModalOpened(): boolean {
    return this.showSelectDateModal || this.showSelectLabelsMenu || 
    this.showSelectPriorityModal || this.showSelectProjectModal ||
    this.taskListSubmodalOpened || this.collabTaskListSubmodalOpened || this.deleteService.isOpened();
  }

  changeTaskListSubmodalState(opened: boolean) {
    this.taskListSubmodalOpened = opened;
  }

  @HostListener("window:keydown.escape", ["$event"])
  handleKeyboardEscapeEvent() {
    if(!this.subModalOpened) {
      this.closeModal();
    }
  }
}
