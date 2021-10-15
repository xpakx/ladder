import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { TaskTreeService } from 'src/app/service/task-tree.service';

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

  constructor(private taskTree: TaskTreeService) { }

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
}
