import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { TaskDetails } from 'src/app/entity/task-details';
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

  constructor(tree: TreeService, private fb: FormBuilder) { }

  ngOnInit(): void {
    this.taskForm = this.fb.group({
      title: [this.task ? this.task.title : '', Validators.required],
      description: [this.task ? this.task.description : '', []]
    });
  }

  closeForm() {
    this.closeEvent.emit(true);
  }
}
