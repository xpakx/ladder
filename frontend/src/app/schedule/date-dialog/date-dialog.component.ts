import { Component, EventEmitter, HostListener, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { DateEvent } from 'src/app/common/utils/date-event';

export interface DateForm {
  date: FormControl<string>;
  time: FormControl<string>;
}

@Component({
  selector: 'app-date-dialog',
  templateUrl: './date-dialog.component.html',
  styleUrls: ['./date-dialog.component.css']
})
export class DateDialogComponent implements OnInit {
  @Output() closeEvent = new EventEmitter<DateEvent>();
  @Output() cancelEvent = new EventEmitter<boolean>();
  @Input() taskDate: Date | undefined;
  @Input() timeboxed: boolean = false;
  dateSelectForm: FormGroup<DateForm> | undefined;

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
    this.dateSelectForm = this.fb.nonNullable.group(
      {
        date: [this.taskDate ? this.formatDate(this.taskDate) : '', Validators.required],
        time: [this.taskDate ? this.formatTime(this.taskDate) : '', []]
      }
    );
  }

  formatDate(date: Date): string {
    return date.toISOString().split("T")[0];
  }

  formatTime(date: Date): string {
    return date.toISOString().split("T")[1];
  }

  closeSelectDateMenu(): void {
    this.closeEvent.emit({ date: this.taskDate, timeboxed: this.timeboxed && this.dateSelectForm?.controls.date.value != '' });
  }

  cancel(): void {
    this.cancelEvent.emit(true);
  }

  chooseDate(date: Date | undefined): void {
    this.taskDate = date;
    this.closeSelectDateMenu();
  } 

  selectDateFromForm(): void {
    if(this.dateSelectForm) {
      this.taskDate = new Date(this.dateSelectForm.controls.date.value);
      if(this.timeboxed && this.dateSelectForm.controls.date.value != '') {
        let date: string[] = this.dateSelectForm.controls.time.value.split(":");
        this.taskDate.setHours(Number(date[0]));
        this.taskDate.setMinutes(Number(date[1]));
      }
    }
    this.closeSelectDateMenu();
  } 

  switchTimeboxed(): void {
    this.timeboxed = !this.timeboxed;
  }

  @HostListener("window:keydown.escape", ["$event"])
  handleKeyboardEscapeEvent(): void {
    this.cancel();
  }

  @HostListener("window:keydown.enter", ["$event"])
  handleKeyboardEnterEvent(): void {
    this.selectDateFromForm();
  }
}
