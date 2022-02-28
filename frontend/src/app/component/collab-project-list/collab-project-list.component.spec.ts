import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CollabProjectListComponent } from './collab-project-list.component';

describe('CollabProjectListComponent', () => {
  let component: CollabProjectListComponent;
  let fixture: ComponentFixture<CollabProjectListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CollabProjectListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CollabProjectListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
