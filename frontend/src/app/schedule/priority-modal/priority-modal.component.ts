import { Component, EventEmitter, HostListener, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-priority-modal',
  templateUrl: './priority-modal.component.html',
  styleUrls: ['./priority-modal.component.css']
})
export class PriorityModalComponent implements OnInit {
  @Output() closeEvent = new EventEmitter<number>();
  @Output() cancelEvent = new EventEmitter<boolean>();
  @Input() priority: number = 0;

  priorities = [
    {priority: 0, name: "No priority"},
    {priority: 1, name: "Low priority"},
    {priority: 2, name: "Medium priority"},
    {priority: 3, name: "High priority"},
  ];

  constructor() { }

  ngOnInit(): void {
  }

  choosePriority(priority: number) {
    this.closeEvent.emit(priority);
  }

  closeModal() {
    this.cancelEvent.emit(true);
  }

  @HostListener("window:keydown.escape", ["$event"])
  handleKeyboardEscapeEvent() {
    this.closeModal();
  }
}
