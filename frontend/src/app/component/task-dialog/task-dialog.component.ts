import { Component, EventEmitter, HostListener, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-task-dialog',
  templateUrl: './task-dialog.component.html',
  styleUrls: ['./task-dialog.component.css']
})
export class TaskDialogComponent implements OnInit {
  @Output() closeEvent = new EventEmitter<boolean>();
  public view: number = 0;

  constructor() { }

  ngOnInit(): void {
  }

  closeAddTaskModal() {
    this.closeEvent.emit(true);
  }

  chooseTab(num: number) {
    this.view = num;
  }
}
