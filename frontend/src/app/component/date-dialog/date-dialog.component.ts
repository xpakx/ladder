import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-date-dialog',
  templateUrl: './date-dialog.component.html',
  styleUrls: ['./date-dialog.component.css']
})
export class DateDialogComponent implements OnInit {
  @Output() closeEvent = new EventEmitter<Date | undefined>();
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

  dateWithinWeek(date: Date): boolean {
    let dateToCompare: Date = new Date();
    dateToCompare.setDate(dateToCompare.getDate() + 9);
    dateToCompare.setHours(0);
    dateToCompare.setMinutes(0);
    dateToCompare.setSeconds(0);
    dateToCompare.setMilliseconds(0);
    return date < dateToCompare && !this.isOverdue(date);
  }

  isOverdue(date: Date): boolean {
    let dateToCompare: Date = new Date();
    dateToCompare.setHours(0);
    dateToCompare.setMinutes(0);
    dateToCompare.setSeconds(0);
    return date < dateToCompare;
  }

  sameDay(date1: Date, date2: Date): boolean {
    return date1.getFullYear() == date2.getFullYear() && date1.getDate() == date2.getDate() && date1.getMonth() == date2.getMonth();
  }

  isToday(date: Date): boolean {
    let today = new Date();
    return this.sameDay(today, date);
  }

  isTomorrow(date: Date): boolean {
    let tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return this.sameDay(tomorrow, date);
  }

  thisYear(date: Date): boolean {
    let today = new Date();
    return today.getFullYear() == date.getFullYear();
  }

  closeSelectDateMenu() {
    this.closeEvent.emit(this.taskDate);
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

}
