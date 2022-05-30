import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SettingsInvitationComponent } from './settings-invitation.component';

describe('SettingsInvitationComponent', () => {
  let component: SettingsInvitationComponent;
  let fixture: ComponentFixture<SettingsInvitationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SettingsInvitationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SettingsInvitationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
