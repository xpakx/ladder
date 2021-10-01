import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LabelChoiceDialogComponent } from './label-choice-dialog.component';

describe('LabelChoiceDialogComponent', () => {
  let component: LabelChoiceDialogComponent;
  let fixture: ComponentFixture<LabelChoiceDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LabelChoiceDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LabelChoiceDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
