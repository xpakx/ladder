import { Component, EventEmitter, HostListener, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-date-dialog',
  templateUrl: './date-dialog.component.html',
  styleUrls: ['./date-dialog.component.css']
})
export class DateDialogComponent implements OnInit {
  @Output() closeEvent = new EventEmitter<Date | undefined>();
  @Output() cancelEvent = new EventEmitter<boolean>();
  @Input() taskDate: Date | undefined;
  dateSelectForm: FormGroup | undefined;

  today: Date;
  tomorrow: Date;
  weekend: Date;
  nextWeek: Date;

  constructor(private fb: FormBuilder) { 
    this.today = new Date();
    let dayOfTheMonth = this.today.getDate();
    let dayOfTheWeek = this.today.getDay();
    this.tomorrow = new Date();
    this.tomorrow.setDate(dayOfTheMonth + 1);
    this.weekend = new Date();
    this.weekend.setDate(dayOfTheMonth - dayOfTheWeek + 6);
    this.nextWeek = new Date(this.weekend);
    this.nextWeek.setDate(this.weekend.getDate() + 2);
  }

  ngOnInit(): void {
    this.dateSelectForm = this.fb.group(
      {
        date: [this.taskDate ? this.formatDate(this.taskDate) : '', Validators.required]
      }
    );
  }

  formatDate(date: Date): String {
    return date.toISOString().split("T")[0];
  }

  closeSelectDateMenu() {
    this.closeEvent.emit(this.taskDate);
  }

  cancel() {
    this.cancelEvent.emit(true);
  }

  chooseDate(date: Date | undefined) {
    this.taskDate = date;
    this.closeSelectDateMenu();
  } 

  selectDateFromForm() {
    if(this.dateSelectForm) {
      this.taskDate = new Date(this.dateSelectForm.controls.date.value);
    }
    this.closeSelectDateMenu();
  } 

  @HostListener("window:keydown.escape", ["$event"])
  handleKeyboardEscapeEvent() {
    this.cancel();
  }

  @HostListener("window:keydown.enter", ["$event"])
  handleKeyboardEnterEvent() {
    this.selectDateFromForm();
  }
}
