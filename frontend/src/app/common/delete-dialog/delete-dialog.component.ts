import { Component, EventEmitter, HostListener, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-delete-dialog',
  templateUrl: './delete-dialog.component.html',
  styleUrls: ['./delete-dialog.component.css']
})
export class DeleteDialogComponent implements OnInit {
  @Output() closeEvent = new EventEmitter<boolean>();
  @Output() deleteEvent = new EventEmitter<number>();
  @Input() name: string = "";
  @Input() id: number | undefined;

  constructor() { }

  ngOnInit(): void {
  }

  cancel(): void {
    this.closeEvent.emit(true);
  }

  delete(): void {
    this.deleteEvent.emit(this.id);
  }

  @HostListener("window:keydown.escape", ["$event"])
  handleKeyboardEscapeEvent(): void {
    this.cancel();
  }

  @HostListener("window:keydown.enter", ["$event"])
  handleKeyboardEnterEvent(): void {
    this.delete();
  }
}
