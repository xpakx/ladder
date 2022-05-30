import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CollabTaskListComponent } from './collab-task-list.component';

describe('CollabTaskListComponent', () => {
  let component: CollabTaskListComponent;
  let fixture: ComponentFixture<CollabTaskListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CollabTaskListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CollabTaskListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
