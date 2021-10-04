import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

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

  cancel() {
    this.closeEvent.emit(true);
  }

  delete() {
    this.deleteEvent.emit(this.id);
  }

}
