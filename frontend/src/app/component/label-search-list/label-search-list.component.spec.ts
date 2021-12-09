import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LabelSearchListComponent } from './label-search-list.component';

describe('LabelSearchListComponent', () => {
  let component: LabelSearchListComponent;
  let fixture: ComponentFixture<LabelSearchListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LabelSearchListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LabelSearchListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
