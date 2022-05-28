import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DateLabelComponent } from './date-label.component';

describe('DateLabelComponent', () => {
  let component: DateLabelComponent;
  let fixture: ComponentFixture<DateLabelComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DateLabelComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DateLabelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
