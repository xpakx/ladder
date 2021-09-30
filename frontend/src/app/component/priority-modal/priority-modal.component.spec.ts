import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PriorityModalComponent } from './priority-modal.component';

describe('PriorityModalComponent', () => {
  let component: PriorityModalComponent;
  let fixture: ComponentFixture<PriorityModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PriorityModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PriorityModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
