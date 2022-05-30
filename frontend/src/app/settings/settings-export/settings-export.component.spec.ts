import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SettingsExportComponent } from './settings-export.component';

describe('SettingsExportComponent', () => {
  let component: SettingsExportComponent;
  let fixture: ComponentFixture<SettingsExportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SettingsExportComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SettingsExportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
