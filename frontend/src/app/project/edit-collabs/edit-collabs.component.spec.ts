import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditCollabsComponent } from './edit-collabs.component';

describe('EditCollabsComponent', () => {
  let component: EditCollabsComponent;
  let fixture: ComponentFixture<EditCollabsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditCollabsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCollabsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
