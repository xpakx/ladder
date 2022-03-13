import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CollabSubtaskListComponent } from './collab-subtask-list.component';

describe('CollabSubtaskListComponent', () => {
  let component: CollabSubtaskListComponent;
  let fixture: ComponentFixture<CollabSubtaskListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CollabSubtaskListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CollabSubtaskListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
