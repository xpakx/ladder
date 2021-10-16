import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Task } from 'src/app/entity/task';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { TaskTreeService } from 'src/app/service/task-tree.service';
import { TaskService } from 'src/app/service/task.service';

@Component({
  selector: 'app-task-view',
  templateUrl: './task-view.component.html',
  styleUrls: ['./task-view.component.css']
})
export class TaskViewComponent implements OnInit {
  @Input("task") parent?: TaskTreeElem;
  @Output() closeEvent = new EventEmitter<boolean>();
  edit: boolean = false;
  parentData!: AddEvent<TaskTreeElem>;

  constructor(private taskTree: TaskTreeService, private taskService: TaskService) { }

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
    this.taskService.completeTask(this.parent.id, {flag: !this.parent.completed}).subscribe(
        (response: Task) => {
        this.taskTree.changeTaskCompletion(response);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
    }
  }
}
