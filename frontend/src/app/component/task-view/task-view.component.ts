import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { TaskTreeService } from 'src/app/service/task-tree.service';

@Component({
  selector: 'app-task-view',
  templateUrl: './task-view.component.html',
  styleUrls: ['./task-view.component.css']
})
export class TaskViewComponent implements OnInit {
  @Input("task") parent?: TaskTreeElem;
  @Output() closeEvent = new EventEmitter<boolean>();

  constructor(private taskTree: TaskTreeService) { }

  

  ngOnInit(): void {
  }

  closeModal() {
    this.closeEvent.emit(true);
  }
}
