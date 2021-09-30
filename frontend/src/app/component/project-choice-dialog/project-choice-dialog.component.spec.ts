import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectChoiceDialogComponent } from './project-choice-dialog.component';

describe('ProjectChoiceDialogComponent', () => {
  let component: ProjectChoiceDialogComponent;
  let fixture: ComponentFixture<ProjectChoiceDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProjectChoiceDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectChoiceDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
