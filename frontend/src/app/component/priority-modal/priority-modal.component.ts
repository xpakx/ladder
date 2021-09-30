import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-priority-modal',
  templateUrl: './priority-modal.component.html',
  styleUrls: ['./priority-modal.component.css']
})
export class PriorityModalComponent implements OnInit {
  @Output() closeEvent = new EventEmitter<number>();
  @Output() cancelEvent = new EventEmitter<boolean>();
  @Input() priority: number = 0;

  constructor() { }

  ngOnInit(): void {
  }

  choosePriority(priority: number) {
    this.closeEvent.emit(priority);
  }

  closeModal() {
    this.cancelEvent.emit(true);
  }
}
