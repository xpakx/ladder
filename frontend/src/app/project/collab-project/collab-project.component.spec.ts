import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CollabProjectComponent } from './collab-project.component';

describe('CollabProjectComponent', () => {
  let component: CollabProjectComponent;
  let fixture: ComponentFixture<CollabProjectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CollabProjectComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CollabProjectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
