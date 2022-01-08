import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectArchiveComponent } from './project-archive.component';

describe('ProjectArchiveComponent', () => {
  let component: ProjectArchiveComponent;
  let fixture: ComponentFixture<ProjectArchiveComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProjectArchiveComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectArchiveComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
