import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SettingsImportComponent } from './settings-import.component';

describe('SettingsImportComponent', () => {
  let component: SettingsImportComponent;
  let fixture: ComponentFixture<SettingsImportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SettingsImportComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SettingsImportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
