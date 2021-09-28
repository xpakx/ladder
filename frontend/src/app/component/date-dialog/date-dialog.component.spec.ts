import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DateDialogComponent } from './date-dialog.component';

describe('DateDialogComponent', () => {
  let component: DateDialogComponent;
  let fixture: ComponentFixture<DateDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DateDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DateDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
