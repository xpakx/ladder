import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectSearchListComponent } from './project-search-list.component';

describe('ProjectSearchListComponent', () => {
  let component: ProjectSearchListComponent;
  let fixture: ComponentFixture<ProjectSearchListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProjectSearchListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectSearchListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
