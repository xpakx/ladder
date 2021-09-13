import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { TaskDetails } from 'src/app/entity/task-details';

@Component({
  selector: 'app-task-form',
  templateUrl: './task-form.component.html',
  styleUrls: ['./task-form.component.css']
})
export class TaskFormComponent implements OnInit {
  @Input() task: TaskDetails | undefined;
  @Input() project: ProjectTreeElem | undefined;
  @Output() closeEvent = new EventEmitter<boolean>();

  constructor() { }

  ngOnInit(): void {
  }

  closeForm() {
    this.closeEvent.emit(true);
  }
}
