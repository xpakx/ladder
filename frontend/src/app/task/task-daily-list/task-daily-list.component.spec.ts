import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TaskDailyListComponent } from './task-daily-list.component';

describe('TaskDailyListComponent', () => {
  let component: TaskDailyListComponent;
  let fixture: ComponentFixture<TaskDailyListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TaskDailyListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskDailyListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
